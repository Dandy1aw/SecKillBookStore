package com.seckill.miaosha.service;

import com.seckill.miaosha.domain.MiaoshaGoods;
import com.seckill.miaosha.domain.MiaoshaOrder;
import com.seckill.miaosha.domain.MiaoshaUser;
import com.seckill.miaosha.domain.OrderInfo;
import com.seckill.miaosha.redis.MiaoshaKey;
import com.seckill.miaosha.redis.RedisService;
import com.seckill.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}
