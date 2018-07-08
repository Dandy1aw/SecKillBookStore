package com.seckill.miaosha.rabbitmq;

import org.springframework.amqp.core.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class MQConfig {

    public static final String MIAOSHA_QUEUE = "MIAOSHA_queue";//实际用过的秒杀消息队列/**/

    public static final String QUEUE = "queue";
    public static final String TOPIC_QUEUE1 = "TOPIC_queue1";
    public static final String TOPOC_QUEUE2 = "TOPIC_queue2";
    public static final String TOPOC_EXCHANGE = "TOPIC_EXCHANGE";
    public static final String FANOUT_EXCHANGE = "FANOUT_EXCHANGE";
    public static final String HEADERS_EXCHANGE = "HEADERS_EXCHANGE";
    public static final String HEADERS_QUEUE = "HEADERS_queue";


    @Bean
    public Queue Miaosha_queue(){
        /** 业务中使用的 秒杀 QUEUE
         **/
        return new Queue(MIAOSHA_QUEUE,true);
    }


    /*需要设置一个Bean 表示获取 Queue对象*/

    /**这是最基础的模式：Direct 模式   Exchange(交换机) 有四种模式
     * 可以使用rabbit mq 自带的default Exchange
     * */
    @Bean
    public Queue queue(){
        /*参数有2个，第一个队列名字，第二个是否持久化*/
        return new Queue(QUEUE,true);
    }

    /**Topic 模式 ：通配模式
     * */
    @Bean
    public Queue topQueue1(){
        /*参数有2个，第一个队列名字，第二个是否持久化*/
        return new Queue(TOPIC_QUEUE1,true);
    }
    @Bean
    public Queue topQueue2(){
        /*参数有2个，第一个队列名字，第二个是否持久化*/
        return new Queue(TOPOC_QUEUE2,true);
    }
    /**Topic 模式必须如下步骤
     *       1.创建交换机
     *       2.绑定Exchange
     * */
    @Bean
    public TopicExchange topicExchange(){
        return new TopicExchange(TOPOC_EXCHANGE);
    }

    @Bean
    public Binding topicBinding1(){
        /*交换机绑定 消息队列 并 适配 routingkey 将符合的 routingkey 消息传传送到 对应的 消息队列*/
        return BindingBuilder.bind(topQueue1()).to(topicExchange()).with("topic.key1");
    }
    @Bean
    public Binding topicBinding2(){
        /*交换机绑定 消息队列 并 适配 routingkey 将符合的 routingkey 消息传传送到 对应的 消息队列*/
        return BindingBuilder.bind(topQueue2()).to(topicExchange()).with("topic.#");// # 表示通配，适配所有字符
    }

    /** Fanout 模式： 广播模式 无需routing key 需要绑定Exchange
     * */
    /*创建交换机*/
    @Bean
    public FanoutExchange fanoutExchange()
    {
        return new FanoutExchange(FANOUT_EXCHANGE);
    }

    @Bean
    public Binding fanoutBinding1(){
        /*交换机绑定 消息队列 并 适配 routingkey 将符合的 routingkey 消息传传送到 对应的 消息队列*/
        return BindingBuilder.bind(topQueue1()).to(fanoutExchange());
    }
    @Bean
    public Binding fanoutBinding2(){
        /*交换机绑定 消息队列 并 适配 routingkey 将符合的 routingkey 消息传传送到 对应的 消息队列*/
        return BindingBuilder.bind(topQueue2()).to(fanoutExchange());// # 表示通配，适配所有字符
    }


    /** Header 模式： 发送的是携带header 信息 会校验 之后确认才会接受
     * */
    /*创建 Headers交换机*/
    @Bean
    public HeadersExchange headersExchange()
    {
        return new HeadersExchange(HEADERS_EXCHANGE);
    }
    @Bean
    public Queue headerQueue(){
        /*参数有2个，第一个队列名字，第二个是否持久化*/
        return new Queue(HEADERS_QUEUE,true);
    }

    @Bean
    public Binding headBing(){
        Map<String,Object> map = new HashMap<String, Object>();
        map.put("head1","111");
        map.put("head2","111");
        /*绑定的时候 会根据一个map 绑定，只有 匹配 whereall 是全匹配，才会发送消息给对应 QUEUE */
        return BindingBuilder.bind(headerQueue()).to(headersExchange()).whereAll(map).match();// # 表示通配，适配所有字符
    }
}
