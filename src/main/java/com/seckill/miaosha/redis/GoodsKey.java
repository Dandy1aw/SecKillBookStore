package com.seckill.miaosha.redis;

/**
 * Created by syw on 2018/6/18.
 */
public class GoodsKey extends BasePrefix {

    /*私有化 构造函数 外面无法创建*/
    public static GoodsKey getGoodsList = new GoodsKey(60,"GoodsList");
    public static GoodsKey getGoodsDetail = new GoodsKey(60,"GoodsDetail");
    public static GoodsKey getGoodsStock = new GoodsKey(0,"GoodsStock");

    private GoodsKey(String prefix) {
        super(prefix);
    }
    private GoodsKey(int time , String prefix){
        super(time,prefix);
    }

}
