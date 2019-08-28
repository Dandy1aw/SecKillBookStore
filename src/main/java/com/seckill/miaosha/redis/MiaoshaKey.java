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
    public static final MiaoshaKey getMiaoshaVerifyCode = new MiaoshaKey(60,"MiaoshaOver");
    public static final MiaoshaKey getMiaoshaFangShua = new MiaoshaKey(60,"MiaoshaFangShua");
    public static MiaoshaKey withExpire(int time){
        return new MiaoshaKey(time,"Fangshua");
    }
}
