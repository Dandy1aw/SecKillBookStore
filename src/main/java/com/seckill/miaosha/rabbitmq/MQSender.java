package com.seckill.miaosha.rabbitmq;

import com.seckill.miaosha.domain.MiaoshaMessage;
import com.seckill.miaosha.redis.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MQSender {

    private static Logger logger = LoggerFactory.getLogger(MQSender.class);
    @Autowired
    AmqpTemplate amqpTemplate; //要用到这个对象的方法

    public void miaoshaSend(MiaoshaMessage miaoshaMessage){
        String msg = RedisService.beanToString(miaoshaMessage);
        logger.info("Send message :"+msg);
        /*秒杀 发送的是 是一个MiaoshaMessage 对象 */
        amqpTemplate.convertAndSend(MQConfig.MIAOSHA_QUEUE,msg);
    }

    /*Dirct模式 send 方法*/
    public void send(Object message){
        String msg = RedisService.beanToString(message);
        logger.info("Send message :"+msg);
        /*发送,最终发送的是一个字符串*/
        amqpTemplate.convertAndSend(MQConfig.QUEUE,msg);
    }

    /*Topic模式 send 方法*/
    public void sendTopic(Object message){
        String msg = RedisService.beanToString(message);
        logger.info("Send message :"+msg);
        /*发送,最终发送的是一个字符串*/
        amqpTemplate.convertAndSend(MQConfig.TOPOC_EXCHANGE,"topic.key1","发给Topic 1:"+msg);
        amqpTemplate.convertAndSend(MQConfig.TOPOC_EXCHANGE,"topic.#","发给Topic 2:"+msg);
    }

    /*Fanout send 方法*/
    public void sendFanout(Object message){
        String msg = RedisService.beanToString(message);
        logger.info("Send message :"+msg);
        /*发送,最终发送的是一个字符串*/
        amqpTemplate.convertAndSend(MQConfig.FANOUT_EXCHANGE,"","Fanout mo模式 :"+msg);
    }

    /*Header send 方法*/
    public void sendHeader(Object message){
        String msg = RedisService.beanToString(message);
        logger.info("Send message :"+msg);
        /*header 模式发送的是一个Message 对象，实际信息发送的是bytes数组*/
        MessageProperties messageProperties = new MessageProperties();
        messageProperties.setHeader("head1","111");
        messageProperties.setHeader("head2","111");
        Message obj = new Message(msg.getBytes(),messageProperties);


        amqpTemplate.convertAndSend(MQConfig.HEADERS_EXCHANGE,"",obj);
    }

}
