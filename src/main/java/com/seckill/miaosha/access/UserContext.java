package com.seckill.miaosha.access;

import com.seckill.miaosha.domain.MiaoshaUser;

/** 定义一个当前线程类 并存放 一个ThreadLocal容器，用于存放 user 对象，好处：user绑定于本线程，没有线程冲突
 * */
public class UserContext {

    public static ThreadLocal<MiaoshaUser> userHolder = new ThreadLocal<MiaoshaUser>();
    /*定义2个方法，一个存一个取*/
    public static void setUser(MiaoshaUser user){
        userHolder.set(user);
    }
    public static MiaoshaUser getUser(){
        return userHolder.get();
    }
}
