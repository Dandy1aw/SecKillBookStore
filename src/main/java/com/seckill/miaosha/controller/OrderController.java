package com.seckill.miaosha.controller;

import com.seckill.miaosha.domain.MiaoshaUser;
import com.seckill.miaosha.domain.OrderInfo;
import com.seckill.miaosha.result.CodeMsg;
import com.seckill.miaosha.result.Result;
import com.seckill.miaosha.service.GoodsService;
import com.seckill.miaosha.service.OrderService;
import com.seckill.miaosha.vo.GoodsVo;
import com.seckill.miaosha.vo.OrderVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/order")
public class OrderController {
    @Autowired
    OrderService orderService;

    @Autowired
    GoodsService goodsService;

    @RequestMapping("/getOrderInfo")
    @ResponseBody
    public Result<OrderVo> getOrderInfo(MiaoshaUser user,
                                        @RequestParam("orderId")long orderId){

        if (user == null){
            return Result.error(CodeMsg.SESSION_ERROR);
        }
        /*用户存在 就用orderId 取出订单详细信息*/
        OrderInfo orderInfo = orderService.getOrderById(orderId);
        if (orderInfo == null)
        {
            return Result.error(CodeMsg.ORDER_NOT_EXIST);
        }
        /*通过订单信息中的商品id 获取商品信息*/
        GoodsVo goodsVo = goodsService.getGoodsById(orderInfo.getGoodsId());
        OrderVo orderVo = new OrderVo();
        orderVo.setGoodsVo(goodsVo);
        orderVo.setOrderInfo(orderInfo);
        /*返回订单相关信息*/
        return Result.success(orderVo);
    }
}
