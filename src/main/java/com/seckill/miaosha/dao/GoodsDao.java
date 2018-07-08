package com.seckill.miaosha.dao;

import com.seckill.miaosha.domain.MiaoshaGoods;
import com.seckill.miaosha.vo.GoodsVo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

/**
 * Created by syw on 2018/6/26.
 */
@Mapper
public interface GoodsDao {

    @Select("select  g.*,mg.miaosha_price,mg.stock_count,mg.start_date,mg.end_date from miaosha_goods mg join goods g on mg.goods_id = g.id")
    List<GoodsVo> getGoodsList();

    @Select("select  g.*,mg.miaosha_price,mg.stock_count,mg.start_date,mg.end_date from miaosha_goods mg join goods g on mg.goods_id = g.id where g.id = #{goodsId}")
    GoodsVo getGoodsById(@Param("goodsId") Long goodsId);

    @Update("update miaosha_goods set stock_count = stock_count - 1 where goods_id = #{goodsId} and stock_count >0")
    int reduceStock(MiaoshaGoods goods);
}
