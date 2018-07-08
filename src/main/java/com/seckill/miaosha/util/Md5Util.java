package com.seckill.miaosha.util;

/**
 * Created by syw on 2018/6/19.
 */
import org.apache.commons.codec.digest.DigestUtils;

import java.lang.ref.SoftReference;

/** 用于生成MD5 密码的工具类
 * */
public class Md5Util {
    public static String md5(String input){
        return DigestUtils.md5Hex(input);//注意导包为codec中的DigestUtils类
    }

    /*固定盐值*/
    private static final  String SALT = "1a2b3c4d";

    /** 第一次md5 ：
     * 用于 通过输入的密码生成 传输的密码 ：方法 通过固定盐值和明文密码之间的拼接在生成md5
     * @param password
     * @return
     */
    public static String inputPassToFormPass(String password){
        String str = ""+SALT.charAt(5)+SALT.charAt(4)+password+SALT.charAt(3)+SALT.charAt(2);
        return md5(str);
    }


    /**
     * 第二次md5 : 通过输入的密码和数据库随机盐值 继续生成 密码
     * @param input
     * @param salt
     * @return
     */
    public static String formPassToDBPass(String input,String salt){
        String str =""+ salt.charAt(0)+salt.charAt(2)+input+salt.charAt(5)+salt.charAt(4);
        return md5(str);
    }


    /**最终调用生成密码的方法
     * */
    public static String inputPassToDBPass(String password,String dbsalt){
        String FirstMd5 = inputPassToFormPass(password);
        String SecondMd5 = formPassToDBPass(FirstMd5,dbsalt);
        return SecondMd5;
    }

    public static void main(String[] args) {
        String pass = "123456";
        String salt = "1a2b3c4d";
        System.out.println(inputPassToDBPass(pass,salt));
    }
}
