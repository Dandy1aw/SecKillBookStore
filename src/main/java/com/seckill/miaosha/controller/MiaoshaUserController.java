package com.seckill.miaosha.controller;

import com.seckill.miaosha.domain.MiaoshaUser;
import com.seckill.miaosha.result.Result;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by syw on 2018/6/23.
 */
@Controller
@RequestMapping("/miaoshauser")
public class MiaoshaUserController {


    /** 用于 压测
     *  该接口仅仅访问 redis 从redis 中取到数据
     * */
    @RequestMapping("/info")
    @ResponseBody
    public Result<MiaoshaUser> info(Model model, MiaoshaUser user){
        return Result.success(user);
    }
}
