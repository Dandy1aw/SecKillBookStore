package com.seckill.miaosha.redis;

public class OrderKey extends BasePrefix {
    private OrderKey(String prefix) {
        super(prefix);
    }

    private OrderKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }
    public static final OrderKey getOrderByUidGid = new OrderKey("OrderKey");

}
