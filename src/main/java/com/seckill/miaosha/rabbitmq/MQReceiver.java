package com.seckill.miaosha.rabbitmq;

import com.seckill.miaosha.domain.Goods;
import com.seckill.miaosha.domain.MiaoshaMessage;
import com.seckill.miaosha.domain.MiaoshaOrder;
import com.seckill.miaosha.domain.MiaoshaUser;
import com.seckill.miaosha.redis.RedisService;
import com.seckill.miaosha.service.GoodsService;
import com.seckill.miaosha.service.MiaoshaService;
import com.seckill.miaosha.service.OrderService;
import com.seckill.miaosha.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


/**消费者
 * */
@Service
public class MQReceiver {

    @Autowired
    RedisService redisService;

    @Autowired
    GoodsService goodsService;

    @Autowired
    OrderService orderService;
    private static Logger log = LoggerFactory.getLogger(MQReceiver.class);
    @Autowired
    MiaoshaService miaoshaService;

    /**监控MiaoshaQueue 消息队列
     * */
    @RabbitListener(queues = MQConfig.MIAOSHA_QUEUE)
    public void miaoShaReceive(String message){
        log.info("Receive message:");
        /* 通过 消息队列接收到的message  将秒杀的message中携带的 user 和 goodsId */
        MiaoshaMessage miaoshaMessage = RedisService.stringToBean(message,MiaoshaMessage.class);
        long goodsId = miaoshaMessage.getGoodsId();
        MiaoshaUser user = miaoshaMessage.getMiaoshaUser();
        GoodsVo goodsVo =goodsService.getGoodsById(goodsId);
        /*这里继续判断库存： 但是判断 的是数据库的库存 因为 前面 redis 预减已经拦截大部分并发请求*/
        long  stock =goodsVo.getStockCount();
        if (stock <= 0){ // 小于等于0 不能是==0 单线程没有问题
            return;//若没有库存就返回
        }
        /*判断重复秒杀
        * */
        MiaoshaOrder miaoshaOrder = orderService.getMiaoshaByUserAndGood(user.getId(),goodsId);
        if (miaoshaOrder != null){
            return;
        }
        /* 都过了 执行秒杀 */
        miaoshaService.miaosha(user,goodsVo);// 改一下，可能 减库存失败，若果失败就返回
    }



//    /**Direct 模式 接收方法
//     * */
//    @RabbitListener(queues = MQConfig.QUEUE)//监听的是这个queue
//    public void receive(String message){
//        /*监听到有消息的时候就打个输出*/
//        log.info("Receive message :"+message);
//    }
//
//    /**Topic 模式 接收方法
//     * */
//    @RabbitListener(queues = MQConfig.TOPIC_QUEUE1)//监听的是这个queue1
//    public void receiveTopic1(String message){
//        /*监听到有消息的时候就打个输出*/
//        log.info("Queue 1   Receive message  :"+message);
//    }
//
//    @RabbitListener(queues = MQConfig.TOPOC_QUEUE2)//监听的是这个queue2
//    public void receiveTopic2(String message){
//        /*监听到有消息的时候就打个输出*/
//        log.info("Queue 2   Receive message  :"+message);
//    }
//
//    /*接受Header 信息*/
//    @RabbitListener(queues = MQConfig.HEADERS_QUEUE)//监听的是这个queue2
//    public void receiveTopic2(byte [] message){
//        /*监听到有消息的时候就打个输出*/
//        log.info("HEader   Queue  Receive message  :"+new String(message));
//    }
}
