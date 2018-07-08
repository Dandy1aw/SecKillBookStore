package com.seckill.miaosha.vo;

/**
 * Created by syw on 2018/6/19.
 */

import com.seckill.miaosha.validator.IsMobile;
import org.hibernate.validator.constraints.Length;
import javax.validation.constraints.NotNull;

/** 该类用于 接收前端 传来的登陆 数据
 *
 *   关键：1.利用 jsr303 进行 参数校验 避免校验代码的耦合和重复
 *    @NotNull 为该字段不可为空
 *    @Length 长度校验
 * */
public class LoginVo {

    @NotNull
    @IsMobile   /*自定义验证*/
    private String mobile;
    @NotNull
    @Length(min = 32)
    private String password;

    @Override
    public String toString() {
        return "LoginVo{" +
                "mobile='" + mobile + '\'' +
                ", password='" + password + '\'' +
                '}';
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
