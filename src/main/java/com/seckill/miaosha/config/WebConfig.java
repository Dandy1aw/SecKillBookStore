package com.seckill.miaosha.config;


import com.seckill.miaosha.access.AccessInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

/**
 * Created by syw on 2018/6/25.
 */

/* 通过 重写 springmvc 配置 方法 达到 无需传递参数的效果 自动获取 user 从session 中*/
@Configuration
public class WebConfig extends WebMvcConfigurerAdapter{
    @Autowired
    private  UserArguementResolver userArguementResolver;

    @Autowired
    AccessInterceptor accessInterceptor;
    /*解析参数*/
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        argumentResolvers.add(userArguementResolver);

    }
    /*注册拦截器*/

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(accessInterceptor);
    }
}
