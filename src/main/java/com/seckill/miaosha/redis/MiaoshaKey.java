package com.seckill.miaosha.redis;

public class MiaoshaKey extends BasePrefix {
    private MiaoshaKey(String prefix) {
        super(prefix);
    }

    private MiaoshaKey(int expireSeconds, String prefix) {
        super(expireSeconds, prefix);
    }


    public static final MiaoshaKey getMiaoshaOver = new MiaoshaKey(0,"MiaoshaOver");
    public static final MiaoshaKey getMiaoshaPath = new MiaoshaKey(60,"MiaoshaOver");

}
