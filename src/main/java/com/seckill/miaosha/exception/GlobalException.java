package com.seckill.miaosha.exception;

/**
 * Created by syw on 2018/6/23.
 */


import com.seckill.miaosha.result.CodeMsg;

/** 处理业务异常
 * */
public class GlobalException extends RuntimeException {

    private CodeMsg codeMsg;

    public GlobalException(CodeMsg codeMsg){
        super(codeMsg.toString()); /*构造函数 super() 一定要写第一行*/
        this.codeMsg = codeMsg;
    }

    public CodeMsg getCodeMsg() {
        return codeMsg;
    }
}
