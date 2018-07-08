package com.seckill.miaosha.vo;

import com.seckill.miaosha.domain.Goods;

import java.util.Date;

/**
 * Created by syw on 2018/6/26.
 */

/** 用一个GoodsVo 类封装 商品信息里面同时 拥有 商品和秒杀商品的信息
 * */
public class GoodsVo extends Goods{
    private Integer stockCount;
    private Date startDate;
    private Date endDate;
    private Double miaoshaPrice;

    public Double getMiaoshaPrice() {
        return miaoshaPrice;
    }

    public void setMiaoshaPrice(Double miaoshaPrice) {
        this.miaoshaPrice = miaoshaPrice;
    }

    public Integer getStockCount() {
        return stockCount;
    }

    public void setStockCount(Integer stockCount) {
        this.stockCount = stockCount;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }
}
