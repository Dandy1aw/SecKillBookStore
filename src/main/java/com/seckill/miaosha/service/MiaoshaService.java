package com.seckill.miaosha.service;

import com.seckill.miaosha.domain.MiaoshaGoods;
import com.seckill.miaosha.domain.MiaoshaOrder;
import com.seckill.miaosha.domain.MiaoshaUser;
import com.seckill.miaosha.domain.OrderInfo;
import com.seckill.miaosha.redis.MiaoshaKey;
import com.seckill.miaosha.redis.RedisService;
import com.seckill.miaosha.vo.GoodsVo;
import com.sun.org.apache.bcel.internal.generic.FADD;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.Random;

@Service
public class MiaoshaService {

    /*自己的service一般用自己的dao,当用到别人的dao 可以引入service*/
    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;
    @Autowired
    RedisService redisService;

    public long getMiaoshaResult(MiaoshaUser user,long goodsId) {

        MiaoshaOrder miaoshaOrder = orderService.getMiaoshaByUserAndGood(user.getId(),goodsId);
        if (miaoshaOrder != null){//不为空，说明秒杀成功 能查到该秒杀订单
            return miaoshaOrder.getOrderId();
        }else {//为空有两种情况，1 还没完成秒杀 2 没秒杀到  从秒杀 业务中判断
            boolean isOver = getGoodsOver(goodsId);
            if (isOver){
                return -1;
            }else {
                return 0;
            }
        }
    }



    /**
     * 重点：秒杀方法 利用事务
     * 1.减库存 2.下订单！
     * @param user
     * @param goodsVo
     * @return
     */
    @Transactional
    public OrderInfo miaosha(MiaoshaUser user, GoodsVo goodsVo) {
        /*减库存*/
        //该方法返回一个int型数值
        boolean succes = goodsService.reduceStock(goodsVo);
        if (succes){//减库存成功
                 /*下订单*/
            return orderService.insertOrder(user,goodsVo);
        }else {//失败 说明 商品已经被秒杀完了
            //秒杀完了，就做一个标记 通过这个标记来判断是不是因为卖完了，而没有记录
            setGoodsOver(goodsVo.getId());
            return null;
        }
    }

    private void setGoodsOver(long goodsId) {
        redisService.set(MiaoshaKey.getMiaoshaOver,""+goodsId,true);
    }


    private boolean getGoodsOver(long goodsId) {//看存不存在
        return redisService.exist(MiaoshaKey.getMiaoshaOver,""+goodsId);
    }

    public boolean checkPath(MiaoshaUser user, long goodsId,String path) {
        if (user == null || path ==null){
            return false;
        }
        String OldPath  =redisService.get(MiaoshaKey.getMiaoshaPath,""+user.getId()+goodsId,String.class);
        return OldPath.equals(path);
    }

    public BufferedImage createVerifyImg(MiaoshaUser user, long goodsId) {
        if (user == null || goodsId <= 0){
            return null;
        }
        /**生成验证码图片的代码
         * */
        int width = 80;//定义 图像宽高
        int height = 32;
        //create the image
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);//生成一个内存中图像对象 ，宽高，类型
        Graphics g = image.getGraphics();//获取图像的graphics 对象，利用它就可以画图
        // set the background color
        g.setColor(new Color(0xDCDCDC));//设置背景颜色
        g.fillRect(0, 0, width, height);//背景颜色的填充
        // draw the border
        g.setColor(Color.black);//黑色的边框
        g.drawRect(0, 0, width - 1, height - 1);
        // create a random instance to generate the codes
        Random rdm = new Random();//随机数
        // make some confusion
        for (int i = 0; i < 50; i++) {
            int x = rdm.nextInt(width);
            int y = rdm.nextInt(height);
            g.drawOval(x, y, 0, 0);//在图片上生成 50个 干扰的点
        }
        // generate a random code
        String verifyCode = generateVerifyCode(rdm); //生成我们的验证码
        g.setColor(new Color(0, 100, 0));//验证码的颜色
        g.setFont(new Font("Candara", Font.BOLD, 24));//字体
        g.drawString(verifyCode, 8, 24);//将 这个String 类型验证码 写在 图片上
        g.dispose();//关掉这个画笔
        //把验证码存到redis中
        int rnd = calc(verifyCode);//计算这个数学公式验证码的值
        //将这个计算的值放入 redis中等待 对比
        redisService.set(MiaoshaKey.getMiaoshaVerifyCode, user.getId()+","+goodsId, rnd);
        //输出图片
        return image;

    }

    /**计算验证码的值
     * */
    private static int calc(String verifyCode) {
        try {
            ScriptEngineManager manager = new ScriptEngineManager();
            ScriptEngine engine = manager.getEngineByName("JavaScript");

            Double b = (Double) engine.eval(verifyCode);
            int rr = (int)Math.round(b);
            return rr;

        }catch(Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private static  char [] chars = {'+','-','*'};
    //生成这个验证码
    private String generateVerifyCode(Random rdm) {
        int num1 = rdm.nextInt(10);
        int num2 = rdm.nextInt(10);
        int num3 = rdm.nextInt(10);//生成三个随机数（10以内）
        char charOne = chars[rdm.nextInt(3)];//生成两个运算符// n 表示 10以内，不包括10的整数 0-9
        char charTwo = chars[rdm.nextInt(3)];
        //对其进行拼接，获得数学表达式
        String verifyCode = ""+num1+charOne+num2+charTwo+num3;
        return verifyCode;
    }

    public boolean checkVerifyCode(MiaoshaUser user, long goodsId, int verifyCode) {
        if (user == null || goodsId <= 0){
            return false;
        }
        //通过传入的用户Id 和 商品Id 取出缓存中的 验证码计算好的值
        Integer value = redisService.get(MiaoshaKey.getMiaoshaVerifyCode, user.getId()+","+goodsId,Integer.class);
        //使用完了之后删除缓存中的 code ,防止下一次出现2 个缓存
        redisService.delete(MiaoshaKey.getMiaoshaVerifyCode, user.getId()+","+goodsId);
        return value != null && value == verifyCode;
    }
}
