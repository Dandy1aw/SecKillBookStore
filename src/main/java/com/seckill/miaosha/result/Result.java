package com.seckill.miaosha.result;

/**
 * Created by syw on 2018/6/16.
 */

/** 定义一个Result类用于封装 json 数据得到返回
 */
public class Result<T> {
    private int code;
    private String message;
    private T data;

    /** 私有化构造函数
     * 不希望用户用告诉函数来调用
     * success 调用的构造函数 入参为 data
     * */
    private Result(T data) {
        this.code = 0;
        this.message = "成功";
        this.data  = data;
    }

    /**error 调用的构造函数 入参为 CodeMsg
     * */
    private Result(CodeMsg cm) {
        if (cm ==null){
            return;
        }
        this.code = cm.getCode();
        this.message = cm.getMsg();
    }

    /** 静态方法 用于访问 success 和 error
     * */
    /**
     * 成功时候调用success
     * 只需要传入 数据进来就可以了
     * 状态码和信息 默认 为0 和 成功
     * */
    public static <T> Result<T> success(T data){
        return new Result<T>(data);
    }

    /**失败时 调用error
     *通过一个异常 CodeMsg 静态方法 设定异常
     * 针对性的访问
     * */
    public static <T> Result<T> error(CodeMsg cm){
        return new Result<T>(cm);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
