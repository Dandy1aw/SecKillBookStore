package com.seckill.miaosha.redis;

/**
 * Created by syw on 2018/6/18.
 */

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/** 通过一个RedisService 提供service 服务
 * */
@Service
public class RedisService {

    @Autowired
    JedisPool jedisPool;

    /**
     *  通过Jedis 获取 数据 1.通过配置建立连接池 2.从连接池中拿取jedis
     * @param key 想要获取的数据的键
     * @param clazz
     * @param <T> 类型
     */
    public <T> T get(BasePrefix prefix, String key, Class<T> clazz)
    {
        /**1.获取jedis 客户端 是一个连接，用完必须释放掉 close()方法 返回 连接池 而不是关闭
         * */
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();/*调用jedisPool 对象的 getResource()方法 拿到 jedis （其实就是一个连接）*/
            /* 生成 新的 带有前缀的 Key*/
            String  realKey = prefix.getPrefix() +key;
            /* 通过 jedis get() 方法 通过key 获取到 值*/
            String str = jedis.get(realKey);
            /*但我们需要的是 T 类型 通过 stringToBean 方法将str 类型转化为T 类型 */
            T  t = stringToBean(str,clazz);
            return t;
        }finally {
            returnToPool(jedis);
        }
    }


    /**
     *  删除缓存
     * @param prefix
     * @param key
     * @return
     */
    public  boolean delete(BasePrefix prefix, String key)
    {

        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();/*调用jedisPool 对象的 getResource()方法 难道 jedis （其实就是一个连接）*/
            /* 生成 新的 带有前缀的 Key*/
            String  realKey = prefix.getPrefix() +key;
            /* 调用jedis方法删除 缓存*/
            long ret = jedis.del(realKey);//删除 行数
            return  ret > 0;
        }finally {
            returnToPool(jedis);
        }
    }

    /**
     *  判断KEY是否存在
     * @param prefix
     * @param key
     * @return
     */
    public  boolean exist(BasePrefix prefix, String key)
    {
        /**1.获取jedis 客户端 是一个连接，用完必须释放掉 close()方法 返回 连接池 而不是关闭
         * */
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();/*调用jedisPool 对象的 getResource()方法 难道 jedis （其实就是一个连接）*/
            /* 生成 新的 带有前缀的 Key*/
            String  realKey = prefix.getPrefix() +key;
            /* 判断该 key 是否存在*/
            return jedis.exists(realKey);
        }finally {
            returnToPool(jedis);
        }
    }

    /**
     *   键自增
     * @param prefix
     * @param key
     * @param clazz
     * @param <T>
     * @return
     */
    public <T> Long incr(BasePrefix prefix, String key)
    {
        /**1.获取jedis 客户端 是一个连接，用完必须释放掉 close()方法 返回 连接池 而不是关闭
         * */
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();/*调用jedisPool 对象的 getResource()方法 难道 jedis （其实就是一个连接）*/
            /* 生成 新的 带有前缀的 Key*/
            String  realKey = prefix.getPrefix() +key;
            /* 判断该 key 是否存在*/
            return jedis.incr(realKey);
        }finally {
            returnToPool(jedis);
        }
    }


    /**
     *   键自减1
     * @param prefix
     * @param key
     * @return
     */
    public Long decr(BasePrefix prefix, String key)
    {
        /**1.获取jedis 客户端 是一个连接，用完必须释放掉 close()方法 返回 连接池 而不是关闭
         * */
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();/*调用jedisPool 对象的 getResource()方法 难道 jedis （其实就是一个连接）*/
            /* 生成 新的 带有前缀的 Key*/
            String  realKey = prefix.getPrefix() +key;
            /* 判断该 key 是否存在*/
            return jedis.decr(realKey);
        }finally {
            returnToPool(jedis);
        }
    }
    /**
     *  set 方法 返回类型 为boolean  成功设置还是失败
     * @param key 想要获取的数据的键
     * @param <T>
     */
    public <T> boolean set(BasePrefix prefix,String key, T value)
    {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();/*调用jedisPool 对象的 getResource()方法 难道 jedis （其实就是一个连接）*/
            /* 通过 jedis set() 方法 设置 值
            * 但是 redis 只能存储 String 类型 或其他 不能存储 T 类型 需要转化 将T 类型 转化为String */
            String str = beanToString(value);
            /* 生成 新的 带有前缀的 Key*/
            String  realKey = prefix.getPrefix() +key;
            /* 若数据为 空直接返回 false */
            if (str == null) return false;
            int seconds = prefix.expireSeconds();
            if (seconds <= 0){
                /*有效期小于0 时，调用set函数 认为 永不过期*/
                jedis.set(realKey,str);
            }else {
                /*当有效期不小于0,设置有效期*/
                jedis.setex(realKey,seconds,str);
            }
            return true;
        }finally {
            returnToPool(jedis);
        }
    }

    /** 为了存储到redis中 将传入的不同类型的数据转化为String
     * */
    public static  <T> String beanToString(T value) {
        /*判断value的类型 并进行转化*/
        if (value == null){
            return null;
        }
        Class<?> clazz = value.getClass();
        if (clazz == int.class || clazz ==Integer.class)
        {
            return ""+value;
        }else if (clazz == long.class || clazz == Long.class)
        {
            return ""+value;
        }else if (clazz == String.class){
            return (String)value;
        }else {
            /*其他类型 默认为Bean对象 利用JSON 转化为json 串*/
            /*利用阿里的 库 fastjson 将对象转化为 json 串  序列化和反序列化*/
            return JSON.toJSONString(value);
        }
    }

    /* 将 字符串转化为 Bean 的方法*/

    /**
     *
     * @param str
     * @param clazz  想要转化的 类型
     * @param <T>
     * @return
     */
    public static  <T> T stringToBean(String str,Class<T> clazz) {
        if (str==null || str.length() == 0 ||clazz ==null) return null;
        /**根据传入的类型， 将 获取的 字符串 转化为我们需要的类型
         * */
        if (clazz == int.class || clazz ==Integer.class)
        {
            return (T) Integer.valueOf(str);
        }else if (clazz == long.class || clazz == Long.class)
        {
            return (T) Long.valueOf(str);
        }else if (clazz == String.class){
            return (T) str;
        }else {
            /*利用阿里的 库 fastjson 将 字符串转化为 对象的java 对象   */
            return JSON.toJavaObject(JSON.parseObject(str),clazz);
        }
    }

    /* 将该 连接释放 重新放入连接池 */
    private void returnToPool(Jedis jedis) {
        if (jedis!= null)
        {
            jedis.close();
        }
    }

}
