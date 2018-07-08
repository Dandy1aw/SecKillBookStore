package com.seckill.miaosha.redis;

/**
 * Created by syw on 2018/6/18.
 */
public class MiaoshaUserKey extends BasePrefix {

    /*私有化 构造函数 外面无法创建*/
    public static MiaoshaUserKey token = new MiaoshaUserKey(3600*24*2,"token");
    public static MiaoshaUserKey getByName = new MiaoshaUserKey("id");

    private MiaoshaUserKey(String prefix) {
        super(prefix);
    }
    private MiaoshaUserKey(int time ,String prefix){
        super(time,prefix);
    }

}
