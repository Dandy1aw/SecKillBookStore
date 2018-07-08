package com.seckill.miaosha.redis;

/**
 * Created by syw on 2018/6/18.
 */

/* 前缀接口 用于 生成 在 redis 中缓存的 前缀*/
public interface KeyPrefix {

    int expireSeconds();//有效时间

    String getPrefix();//获取前缀的方法
}
