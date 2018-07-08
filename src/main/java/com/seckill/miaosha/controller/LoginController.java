package com.seckill.miaosha.controller;

import com.seckill.miaosha.result.Result;
import com.seckill.miaosha.service.MiaoshaUserService;
import com.seckill.miaosha.vo.LoginVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

/**
 * Created by syw on 2018/6/19.
 */

@RequestMapping("/login")
@Controller
public class LoginController {
    // 获取 日志对象
    private static final Logger log = LoggerFactory.getLogger(LoginController.class);

    @Autowired
    private MiaoshaUserService miaoshaUserService;

    /** 登陆界面
     * @return
     */
    @RequestMapping("/to_login")
    public String login() {
        return "login";//直接去找login.html
    }


    /**异步提交 ajax
     * */
    @RequestMapping("/do_Login")
    @ResponseBody //这个注解有点厉害  可以将返回值 转化为json 格式 输入 也可以转化为json?
    public Result<Boolean> do_Login(HttpServletResponse response, @Valid LoginVo loginVo) {
        log.info(loginVo.toString());
//        /*根据前端传入的手机号和密码 进行 判断 登陆*/
//        String mobile = loginVo.getMobile();
//        String password = loginVo.getPassword();


        /**参数校验：手机号 密码是否为空 是否格式问题        /// 若使用 JSR303参数校验就 省去 此处代码 结合全局异常处理实现 返回
         * */
//        if (password.isEmpty() || password.length() == 0){/*密码为空的时候返回一个 异常 信息*/
//            return Result.error(CodeMsg.LOGIN_ERROR_PASSWORD_EMPTY);
//        }
//        if (mobile.isEmpty() || mobile.length() == 0){/*密码为空的时候返回一个 异常 信息*/
//            return Result.error(CodeMsg.LOGIN_ERROR_MOBILE_EMPTY);
//        }
//        if (!VaildataUtil.isMobile(mobile)){
//            return Result.error(CodeMsg.LOGIN_ERROR_MOBILE_ERROR);
//        }

        /*当参数校验通过时，进行登录验证操作*/
        /** 通过Service 的login方法 验证 登陆条件
         * 若不成功 返回的为CodeMsg error 值
         *  成功 返回CodeMsg success 值
         *
         * */
        miaoshaUserService.login(response,loginVo);
        /**下面是根据service 中业务返回值来判断 CodeMsg 并输出
         * 实际开发中controller 不可以涉及业务代码
         * 若是利用全局异常来处理这部分逻辑
         * 即 若是不能成功登陆就抛出异常
         * 而不需要 根据codemsg来输出
         * 成功直接登陆 就可以
         * 那么就省去这部分逻辑代码
         * */
//        if (cm.getCode() == 0)
//        {
//            return Result.success(true);//通过code 返回给前端一个Result 对象
//        }else {
//            return Result.error(cm);
//        }
        return Result.success(true);
    }
}
