package com.seckill.miaosha.validator;

import com.seckill.miaosha.util.VaildataUtil;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Created by syw on 2018/6/23.
 */
public class IsMobileValidator implements ConstraintValidator<IsMobile,String> {

    /*用于判断该字段是否必填*/
    private boolean required;
    @Override
    public void initialize(IsMobile isMobile) {
        /*拿到注解中的required值*/
        this.required = isMobile.required();
    }
    /*对值进行真正的校验*/
    @Override
    public boolean isValid(String s, ConstraintValidatorContext constraintValidatorContext) {
        /*如果是必填 就说明有值*/
        if (required){
            /*有值就对值进行验证 返回*/
            return VaildataUtil.isMobile(s);
        }else {
            if (s.isEmpty()){
                /*若可以不填,那么验证是为空。空返回true*/
                return true;
            }else {
                /*若不为空 说明有值，验证 */
                return VaildataUtil.isMobile(s);
            }
        }
    }
}
