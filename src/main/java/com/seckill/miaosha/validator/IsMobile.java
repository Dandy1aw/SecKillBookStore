package com.seckill.miaosha.validator;

/**
 * Created by syw on 2018/6/23.
 */
//引入 注解相关？
@java.lang.annotation.Target({java.lang.annotation.ElementType.METHOD, java.lang.annotation.ElementType.FIELD, java.lang.annotation.ElementType.ANNOTATION_TYPE, java.lang.annotation.ElementType.CONSTRUCTOR, java.lang.annotation.ElementType.PARAMETER})
@java.lang.annotation.Retention(java.lang.annotation.RetentionPolicy.RUNTIME)
@java.lang.annotation.Documented
@javax.validation.Constraint(validatedBy = {IsMobileValidator.class}) /*传入 校验器*/
public @interface IsMobile{
    /*默认必须 有值*/
    boolean required() default true;
    /*如果校验不通过 输出 信息*/
    java.lang.String message() default "手机号码格式不对啊！";

    java.lang.Class<?>[] groups() default {};

    java.lang.Class<? extends javax.validation.Payload>[] payload() default {};
}
