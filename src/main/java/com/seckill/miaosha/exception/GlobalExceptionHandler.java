package com.seckill.miaosha.exception;

import com.seckill.miaosha.result.CodeMsg;
import com.seckill.miaosha.result.Result;
import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * Created by syw on 2018/6/23.
 */
/**该注解会 适用所有的@RequestMapper() 结合@ExceptionHander 实现全局异常处理
 *  */
@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {
    @ExceptionHandler(value = Exception.class) /*定义拦截 异常的范围 此时 是拦截所有异常*/
    public Result<String> exceptionHandler(HttpServletRequest request,Exception e){
        e.printStackTrace();
        if (e instanceof GlobalException){
            GlobalException globalException = (GlobalException)e;
            return Result.error(globalException.getCodeMsg());
        }else if (e instanceof BindException){
            /*注意：此处的BindException 是 Spring 框架抛出的Validation异常*/
            BindException ex = (BindException)e;
            List<ObjectError> errors = ex.getAllErrors();/*抛出异常可能不止一个 输出为一个List集合*/
            ObjectError error = errors.get(0);/*取第一个异常*/
            String errorMsg = error.getDefaultMessage(); /*获取异常信息*/
            return Result.error(CodeMsg.BIND_ERROR.fillArgs(errorMsg));
        }else {
            return Result.error(CodeMsg.SERVER_ERROR);
        }
    }
}
