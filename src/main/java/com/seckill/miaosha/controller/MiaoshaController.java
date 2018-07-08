package com.seckill.miaosha.controller;

import com.seckill.miaosha.domain.*;
import com.seckill.miaosha.rabbitmq.MQSender;
import com.seckill.miaosha.redis.GoodsKey;
import com.seckill.miaosha.redis.MiaoshaKey;
import com.seckill.miaosha.redis.RedisService;
import com.seckill.miaosha.result.CodeMsg;
import com.seckill.miaosha.result.Result;
import com.seckill.miaosha.service.GoodsService;
import com.seckill.miaosha.service.MiaoshaService;
import com.seckill.miaosha.service.OrderService;
import com.seckill.miaosha.util.Md5Util;
import com.seckill.miaosha.util.UUIDUtill;
import com.seckill.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/miaosha")
public class MiaoshaController implements InitializingBean {
    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;

    @Autowired
    MiaoshaService miaoshaService;

    @Autowired
    RedisService redisService;

    @Autowired
    MQSender sender;

    private Map<Long,Boolean> isOverMap = new HashMap<Long, Boolean>();


    /**安全优化之 ---接口地址随机化(隐藏)
     *           1.点击秒杀之后，先访问该接口生成一个pathId，并存入redis 返回前端
     *           2.前端带着这个pathId去访问秒杀接口，如果传入的path和从redis取出的不一致，就认为 非法请求
     * */
    @RequestMapping(value = "/getPath",method = RequestMethod.GET)
    @ResponseBody
    public Result<String> getPath(MiaoshaUser user, Model model,
                               @RequestParam("goodsId")long goodsId) {
        if (user == null) {
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        String str = UUIDUtill.uuid();
        /*随机生成 一个 pathId 返回给前端*/
        String pathId = Md5Util.md5(str+"1111");
        redisService.set(MiaoshaKey.getMiaoshaPath,""+user.getId()+goodsId,pathId);
        return Result.success(pathId);
    }

    /**秒杀接口优化之---   第一步:  系统初始化后就将所有商品库存放入 缓存
     * */
    @Override
    public void afterPropertiesSet() throws Exception {
        List<GoodsVo> goods = goodsService.getGoodsList();
        if (goods == null){
            return;
        }
        for (GoodsVo goodsVo :goods){
            redisService.set(GoodsKey.getGoodsStock,""+goodsVo.getId(),goodsVo.getStockCount());
            isOverMap.put(goodsVo.getId(),false);//先初始化 每个商品都是false 就是还有
        }
    }

    /**秒杀轮询方法： 判断秒杀是否成功： 1.秒杀成功返回 orderId 订单的ID
     *                                 2.秒杀失败 两种情况： 1.秒杀还没结束 还在进行中  返回 0
     *                                                      2.秒杀失败 就是秒杀商品没了 返回 -1
     * */
    @RequestMapping(value = "/result",method = RequestMethod.GET)
    @ResponseBody
    public Result<Long> result(MiaoshaUser user, Model model,
                                   @RequestParam("goodsId")long goodsId){
        if (user==null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        //秒杀成功返回商品Id,失败 2中返回
        long result = miaoshaService.getMiaoshaResult(user,goodsId);
        return Result.success(result);
    }

    /**秒杀：
     * 逻辑：
     * 1.获取用户 若未登录跳转登陆界面
     * 2.判断库存
     * 3.判断是否重复秒杀
     * 4.执行秒杀 service
     * 5.得到订单对象 进入订单页面
     * */
    /**优化：ajax 异步访问接口 返回值为Json 传给前端 用于静态显示 而不是由 框架生成模板并渲染
     * */
    @RequestMapping(value = "/{path}/do_miaosha",method = RequestMethod.POST)// 只允许POST 提交
    @ResponseBody
    public Result<Integer> Miaosha(MiaoshaUser user, Model model,
                          @RequestParam("goodsId")long goodsId,
                                   @PathVariable("path")String path){
        /*判断是否登陆*/
        if (user == null|| path == null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        boolean right = miaoshaService.checkPath(user,goodsId,path);
        /*check PATH id */
        if (!right){
            return Result.error(CodeMsg.REQUEST_ERROR);
        }
        /**再优化： 优化 库存之后的请求不访问redis 通过判断 对应 map 的值
         * */
        boolean isOver = isOverMap.get(goodsId);
        if (isOver){
            return Result.error(CodeMsg.MIAO_SHA_NO_STOCK);
        }
        /**秒杀接口优化之 ----第二步： 预减库存 从缓存中减库存
         * 利用 redis 中的方法，减去库存，返回值为 减去1 之后的值
         * */
        long stock = redisService.decr(GoodsKey.getGoodsStock,""+goodsId);
        /*这里判断不能小于等于，因为减去之后等于 说明还有是正常范围*/
        if (stock<0){
            isOverMap.put(goodsId,true);//没有库存就设置 对应id 商品的map 为true
            return Result.error(CodeMsg.MIAO_SHA_NO_STOCK);
        }
        /*判断重复秒杀 -----这里的方法 也是加入的缓存 所以不需要 优化*/
        MiaoshaOrder miaoshaOrder = orderService.getMiaoshaByUserAndGood(user.getId(),goodsId);
        if (miaoshaOrder != null)
        {
            return Result.error(CodeMsg.MIAO_SHA_REPEAT);/*重复秒杀*/
        }
        /**秒杀接口优化之 ----第三步： 消息队列 异步下单
         *         1.将用户信息和商品Id 封装到 MiaoshaMessage
         *         2.发送Message
         *         3.返回前端一个0 ：表示"排队中"
         * */
        MiaoshaMessage mm =new MiaoshaMessage();
        mm.setMiaoshaUser(user);
        mm.setGoodsId(goodsId);
        sender.miaoshaSend(mm);
        return Result.success(0);// 0 表示等待中 ，排队

        /* 接口优化之 -------未优化之前：*/
//        GoodsVo goodsVo = goodsService.getGoodsById(goodsId);
//        int stock  = goodsVo.getStockCount();
//        if (stock <= 0){ // 小于等于0 不能是==0 单线程没有问题
//            return Result.error(CodeMsg.MIAO_SHA_NO_STOCK);
//        }
//        /**优化：可以
//         * */
//        /*判断是否重复秒杀*/
//        MiaoshaOrder miaoshaOrder = orderService.getMiaoshaByUserAndGood(user.getId(),goodsId);
//        if (miaoshaOrder != null){
//            return Result.error(CodeMsg.MIAO_SHA_REPEAT);
//        }
//        /*都过了.执行秒杀*/
//        /**执行秒杀： 事务 用 秒杀service 完成
//         *
//         * */
//        OrderInfo orderInfo = miaoshaService.miaosha(user,goodsVo);
//        return Result.success(orderInfo);
    }



//    @RequestMapping(value = "/do_miaosha")
//    @ResponseBody
//    public String Miaosha(MiaoshaUser user, Model model,
//                                     @RequestParam("goodsId")long goodsId){
//        /*判断是否登陆*/
//        if (user == null){
//            return "login";
//        }
//        GoodsVo goodsVo = goodsService.getGoodsById(goodsId);
//        int stock  = goodsVo.getStockCount();
//        if (stock <= 0){ // 小于等于0 不能是==0 单线程没有问题
//            model.addAttribute("errMsg",CodeMsg.MIAO_SHA_NO_STOCK.getMsg());
//            return "miaosha_fail";/*返回到秒杀失败页面*/
//        }
//        /*判断是否重复秒杀*/
//        MiaoshaOrder miaoshaOrder = orderService.getMiaoshaByUserAndGood(user.getId(),goodsId);
//        if (miaoshaOrder != null){
//            model.addAttribute("errMsg",CodeMsg.MIAO_SHA_REPEAT.getMsg());
//            return "miaosha_fail";
//        }
//        /*都过了.执行秒杀*/
//        /**执行秒杀： 事务 用 秒杀service 完成
//         *
//         * */
//        OrderInfo orderInfo = miaoshaService.miaosha(user,goodsVo);
//        model.addAttribute("orderInfo",orderInfo);/* 将订单 信息写入 域中*/
//        model.addAttribute("goods",goodsVo);/*商品信息 也写入*/
//        return "order_detail";
//    }
}
