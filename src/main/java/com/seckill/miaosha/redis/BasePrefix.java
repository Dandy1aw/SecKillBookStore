package com.seckill.miaosha.redis;

/**
 * Created by syw on 2018/6/18.
 */

/** 抽象类  作为 前缀类的父类用于继承
 * */
public abstract class BasePrefix implements KeyPrefix {

    private int expireSeconds;

    private String prefix;

    public BasePrefix(String prefix) {
        this.expireSeconds = 0;
        this.prefix = prefix;
    }

    public BasePrefix(int expireSeconds, String prefix) {
        this.expireSeconds = expireSeconds;
        this.prefix = prefix;
    }

    @Override
    public int expireSeconds() {
        return expireSeconds;// 定义 0 为用永不过期
    }

    @Override
    public String getPrefix() {
        String className = this.getClass().getSimpleName();
        return className + "_"+ prefix;
    }
}
