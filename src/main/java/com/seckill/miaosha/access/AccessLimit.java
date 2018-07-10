package com.seckill.miaosha.access;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


/**定义一个注解 ： 用于 限流作用（在固定时间内限制访问次数）
 * 降低代码复杂度和冗余度 提高复用性
 * */
@Retention(RetentionPolicy.RUNTIME)//运行期间有效
@Target(ElementType.METHOD)//注解类型为方法注解
public @interface AccessLimit {
    int seconds(); //固定时间
    int maxCount();//最大访问次数
    boolean needLogin() default true;// 用户是否需要登录
}
