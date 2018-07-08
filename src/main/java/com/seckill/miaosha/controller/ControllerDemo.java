package com.seckill.miaosha.controller;

import com.seckill.miaosha.domain.User;
import com.seckill.miaosha.rabbitmq.MQSender;
import com.seckill.miaosha.redis.RedisService;
import com.seckill.miaosha.redis.MiaoshaUserKey;
import com.seckill.miaosha.result.CodeMsg;
import com.seckill.miaosha.result.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Created by syw on 2018/6/16.
 */

/**
 * 对于 controller 输出有 2种
 * 1.json 数据输出 api  2. html 输出
 */
@RequestMapping("/demo")
@Controller
public class ControllerDemo {
    @Autowired
    private RedisService redisService;

    @Autowired
    MQSender mqSender;
    /*json*/
    @RequestMapping("/success")
    @ResponseBody
    public Result<String> index() {
        return Result.success("this data is success");
    }

    @RequestMapping("/error")
    @ResponseBody
    public Result<String> error() {
        return Result.error(CodeMsg.SUCCESS);
    }

    /*html 利用thymeleaf*/
    @RequestMapping(value = "/show", method = RequestMethod.GET)
    public String showpage(Model model) {
        model.addAttribute("name", " 为了 胜利！！！！");
        return "demo";
    }


    /**消息队列： 用的时候 发送消息需要调用才会发送指定消息
     *           但是接收消息只需要监听 固定的队列即可 一旦有消息来，就可以获取
     *           Direct 模式
     * */
    @RequestMapping("/mq")
    @ResponseBody
    public Result<String> mqDemo(){
        mqSender.send("hello 今晚吃鸡！");
        return Result.success("收到！");
    }

    /**消息队列： Topic 模式收发
     *
     * */
    @RequestMapping("/mq/topic")
    @ResponseBody
    public Result<String> mqDemoTopic(){
        mqSender.sendTopic("hello 今晚吃鸡！");
        return Result.success("收到！");
    }

    /**消息队列： Fanout 模式收发
     *
     * */
    @RequestMapping("/mq/fanout")
    @ResponseBody
    public Result<String> mqDemoFanout(){
        mqSender.sendFanout("hello 今晚吃鸡！");
        return Result.success("收到！");
    }

    /**消息队列： Headers 模式收发
     *
     * */
    @RequestMapping("/mq/headers")
    @ResponseBody
    public Result<String> mqDemoHeader(){
        mqSender.sendHeader("hello 今晚吃鸡！");
        return Result.success("收到！");
    }
//    @RequestMapping("/get")
//    @ResponseBody
//    public Result<User> get() {
//        int id = 12;
////        User user = userserivce.getByid(id);
//        return Result.success(user);
//    }

//    @RequestMapping("/tx")
//    @ResponseBody
//    public Result<Boolean> insert() {
//        boolean u1 = userserivce.insert();
//        return Result.success(u1);
//        }


//    @RequestMapping("/redis/get")
//    @ResponseBody
//    public Result<String> redisGet() {
//        String str = redisService.get(MiaoshaUserKey.getById,""+1,String.class);
//        return Result.success(str);
//    }
//
//
//    @RequestMapping("/redis/set")
//    @ResponseBody
//    public Result<String> redisSet() {
//        boolean b1 = redisService.set(MiaoshaUserKey.getById,""+1,"Hi, wo de tian na !");
//        String str = redisService.get(MiaoshaUserKey.getById,""+1,String.class);
//        return Result.success(str);
//    }
//
//    @RequestMapping("/redis/uset")
//    @ResponseBody
//    public Result<User> UserSet() {
//        User user = new User(1,"11111");
//        boolean b1 = redisService.set(MiaoshaUserKey.getById,""+1,user);
//        User user1 = redisService.get(MiaoshaUserKey.getById,""+1,User.class);
//        return Result.success(user1);
//    }
}



