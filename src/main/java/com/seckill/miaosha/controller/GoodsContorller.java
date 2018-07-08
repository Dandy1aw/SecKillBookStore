package com.seckill.miaosha.controller;

import com.seckill.miaosha.domain.MiaoshaUser;
import com.seckill.miaosha.redis.GoodsKey;
import com.seckill.miaosha.redis.RedisService;
import com.seckill.miaosha.result.Result;
import com.seckill.miaosha.service.GoodsService;
import com.seckill.miaosha.service.MiaoshaUserService;
import com.seckill.miaosha.vo.GoodsDetailVo;
import com.seckill.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.thymeleaf.spring4.context.SpringWebContext;
import org.thymeleaf.spring4.view.ThymeleafViewResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * Created by syw on 2018/6/23.
 */
@Controller
@RequestMapping("/goods")
public class GoodsContorller {
    @Autowired
    private MiaoshaUserService miaoshaUserService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private GoodsService goodsService;

    @Autowired
    ThymeleafViewResolver thymeleafViewResolver;

    @Autowired
    ApplicationContext applicationContext;
    /*跳转商品列表页 并携带 session信息*/
    /**优化： 对于经常访问的页面并且信息不经常变化的页面 引入 页面缓存的技术 降低服务器 压力
     * 在 goods_list 页面加入 页面缓存
     * 1.每次访问从redis 缓存中取 html 源代码 显示
     * 2.若没有，后端手动渲染 页面 并返回给response 输出 ，并将其存入缓存由下次使用
     * */
    /**QPS: 未优化前 380/sec
     * */
    @ResponseBody
    @RequestMapping(value = "/to_list",produces = "text/html")
    public String to_list(HttpServletResponse response, HttpServletRequest request,Model model, MiaoshaUser user){//为了方便手机端 移动端，将参数放在请求中
        /*页面缓存*/
        String html = redisService.get(GoodsKey.getGoodsList,"",String.class);
        if (!StringUtils.isEmpty(html)){//从redis 中取, 取得到返回，取不到 手动渲染
            return html;
        }
            /**这一部分本来是用来从request 和cookie 中获取到session 中的 user 数据
             * 由于每次都要传入 数据并做判断 再获取 user 对象
             * 造成代码 冗余
             * 可以直接 重写SpringMvc 配置中 参数解析 AddArguementResolver()方法，让其遍历 参数
             * 并注入 到 controller 中（我们的user 参数）
             * */
//        if (StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken))
//        {/*如果 cookie 中都没有值 返回 登陆界面*/
//            return "login";
//        }
//        /*有限从paramToken 中取出 cookie值 若没有从 cookieToken 中取*/
//        String token = StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
//        MiaoshaUser user = miaoshaUserService.getByToken(response,token);
        model.addAttribute("user",user);
        List<GoodsVo> goodsList = goodsService.getGoodsList();
        model.addAttribute("goodsList",goodsList);
//        return "goods_list";
        /*手动渲染 利用Thymeleaf 的 ThymeleafViewResolver*/
        SpringWebContext ctx = new SpringWebContext(request,response,request.getServletContext(),request.getLocale(),
                model.asMap(), applicationContext); // model 就是将参数存入 ，其中的所有参数 都是为了将页面渲染出来 放入其中，在返回一个静态的html源码
        /*利用 getTemplateEngine()方法的process() 方法，需要传入模板名称和context 变量*/
        html = thymeleafViewResolver.getTemplateEngine().process("goods_list",ctx);//ctx + 模板 返回源码
        /*得到手动渲染的模板*/
        if (!StringUtils.isEmpty(html)){    //不是空，存入缓存
            redisService.set(GoodsKey.getGoodsList,"",html);
        }
        return html;
    }

    /**
     * 不用静态化的 detail 页面
     */
//    @RequestMapping(value = "/to_detail2/{goodsId}",produces = "text/html") // 前端传入的参数 goodsId
//    @ResponseBody
//    public String detail2(HttpServletRequest request,HttpServletResponse response,Model model,MiaoshaUser user,
//                         @PathVariable("goodsId") Long goodsId){//通过注解@PathVariable获取路径参数
//        /*取缓存*/
//        String html = redisService.get(GoodsKey.getGoodsDetail,""+goodsId,String.class);
//        if (!StringUtils.isEmpty(html))
//        {
//            return html;
//        }
//        //没有缓存，渲染页面
//        /*先将user 传进去 用来判断是否登录*/
//        model.addAttribute("user",user);
//        /*根据传入的Id 通过service 拿到对应的Good信息*/
//        GoodsVo goods = goodsService.getGoodsById(goodsId);
//        model.addAttribute("goods",goods);
//
//        long startTime = goods.getStartDate().getTime();
//        long endTime = goods.getEndDate().getTime();
//        long nowTime = System.currentTimeMillis();/* 拿到现在时间的毫秒值*/
//        /**这里要做一个秒杀时间的判断 秒杀开始 秒杀结束 秒杀进行
//         * */
//        int miaoshaStatus = 0;/*用该变量来表示 秒杀的状态 0 表示秒杀未开始 1 开始 2 结束*/
//        int remainSeconds = 0; /*表示剩余时间 距离秒杀开始的时间*/
//        if (nowTime<startTime){//秒杀未开始
//            miaoshaStatus = 0;
//            remainSeconds = (int)((startTime-nowTime)/1000);//注意此时是 毫秒值 要除以1000
//        }else if (endTime<nowTime){//秒杀结束
//            miaoshaStatus = 2;
//            remainSeconds = -1;
//        }else {//秒杀进行中
//            miaoshaStatus = 1;
//            remainSeconds = 0;
//        }
//        model.addAttribute("remainSeconds",remainSeconds);
//        model.addAttribute("miaoshaStatus",miaoshaStatus);
////        return "goods_detail";
//
//        SpringWebContext ctx = new SpringWebContext(request,response,request.getServletContext(),request.getLocale(),model.asMap(),applicationContext);
//        html = thymeleafViewResolver.getTemplateEngine().process("goods_detail",ctx);
//        if (!StringUtils.isEmpty(html)){
//            redisService.set(GoodsKey.getGoodsDetail,""+goodsId,html);
//        }
//        return html;
//    }

    /** 页面静态化：商品详情页面
     * 方法：返回的是一个今天html 页面 + 利用ajax(通过接口)从服务端获取对应数据 + js技术将数据放入html
     * */
    @RequestMapping(value = "/to_detail/{goodsId}") // 前端传入的参数 goodsId
    @ResponseBody
    public Result<GoodsDetailVo> detail(HttpServletRequest request, HttpServletResponse response, Model model, MiaoshaUser user,
                         @PathVariable("goodsId") Long goodsId){//通过注解@PathVariable获取路径参数
        /*先将user 传进去 用来判断是否登录*/
        model.addAttribute("user",user);
        /*根据传入的Id 通过service 拿到对应的Good信息*/
        GoodsVo goods = goodsService.getGoodsById(goodsId);
        model.addAttribute("goods",goods);

        long startTime = goods.getStartDate().getTime();
        long endTime = goods.getEndDate().getTime();
        long nowTime = System.currentTimeMillis();/* 拿到现在时间的毫秒值*/
        /**这里要做一个秒杀时间的判断 秒杀开始 秒杀结束 秒杀进行
         * */
        int miaoshaStatus = 0;/*用该变量来表示 秒杀的状态 0 表示秒杀未开始 1 开始 2 结束*/
        int remainSeconds = 0; /*表示剩余时间 距离秒杀开始的时间*/
        if (nowTime<startTime){//秒杀未开始
            miaoshaStatus = 0;
            remainSeconds = (int)((startTime-nowTime)/1000);//注意此时是 毫秒值 要除以1000
        }else if (endTime<nowTime){//秒杀结束
            miaoshaStatus = 2;
            remainSeconds = -1;
        }else {//秒杀进行中
            miaoshaStatus = 1;
            remainSeconds = 0;
        }
        model.addAttribute("remainSeconds",remainSeconds);
        model.addAttribute("miaoshaStatus",miaoshaStatus);
        /*
        将我们需要的数据 封装到GoodsDetailVo中
         */
        GoodsDetailVo goodsDetailVo = new GoodsDetailVo();
        goodsDetailVo.setGoodsVo(goods);
        goodsDetailVo.setMiaoshaStatus(miaoshaStatus);
        goodsDetailVo.setRemainSeconds(remainSeconds);
        goodsDetailVo.setUser(user);
        return Result.success(goodsDetailVo);
    }
}
