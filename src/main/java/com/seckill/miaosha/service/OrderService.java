package com.seckill.miaosha.service;


import com.seckill.miaosha.dao.OrderDao;
import com.seckill.miaosha.domain.MiaoshaOrder;
import com.seckill.miaosha.domain.MiaoshaUser;
import com.seckill.miaosha.domain.OrderInfo;
import com.seckill.miaosha.redis.OrderKey;
import com.seckill.miaosha.redis.RedisService;
import com.seckill.miaosha.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
public class OrderService {

    @Autowired
    OrderDao orderDao;

    @Autowired
    RedisService redisService;

    /**
     * 该方法返回一个秒杀Order 对象
     * @param userId
     * @param goodsId
     * @return
     */
    public MiaoshaOrder getMiaoshaByUserAndGood(long userId, long goodsId) {
//        return orderDao.getMiaoshaOrderByUserAndGoods(userId, goodsId);
        return redisService.get(OrderKey.getOrderByUidGid,userId+"-"+goodsId,MiaoshaOrder.class);
    }

    /**下订单方法： 返回一个订单对象 并且 要向2个order表中都加入信息
     * orderinfo 以及 miaosha_order
     * @param user
     * @param goodsVo
     * @return
     */
    @Transactional
    public OrderInfo insertOrder(MiaoshaUser user, GoodsVo goodsVo) {
        OrderInfo orderInfo = new OrderInfo();
        /*设置订单信息*/
        orderInfo.setGoodsId(goodsVo.getId());
        orderInfo.setCreateDate(new Date());
        orderInfo.setUserId(user.getId());
        orderInfo.setDeliveryAddrId(1L);
        orderInfo.setGoodsCount(1);
        orderInfo.setGoodsName(goodsVo.getGoodsName());
        orderInfo.setOrderChannel(1);
        orderInfo.setGoodsPrice(goodsVo.getGoodsPrice());
        orderInfo.setStatus(0);
        /*插入 订单信息 order_info*/
        orderDao.insertOrder(orderInfo);
        /*插入秒杀订单信息*/
        MiaoshaOrder miaoshaOrder = new MiaoshaOrder();
        miaoshaOrder.setGoodsId(goodsVo.getId());
        miaoshaOrder.setOrderId(orderInfo.getId());
        miaoshaOrder.setUserId(user.getId());
        orderDao.insertMiaoshaOrder(miaoshaOrder);
        /**创建订单之后将订单加入缓存
         * */
        redisService.set(OrderKey.getOrderByUidGid,user.getId()+"-"+goodsVo.getId(),miaoshaOrder);
        return orderInfo;
    }

    public OrderInfo getOrderById(long orderId) {
        return orderDao.getOrderById(orderId);
    }
}
