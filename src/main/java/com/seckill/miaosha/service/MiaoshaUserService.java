package com.seckill.miaosha.service;

import com.seckill.miaosha.dao.MiaoshaUserDao;
import com.seckill.miaosha.domain.MiaoshaUser;
import com.seckill.miaosha.exception.GlobalException;
import com.seckill.miaosha.redis.MiaoshaUserKey;
import com.seckill.miaosha.redis.RedisService;
import com.seckill.miaosha.result.CodeMsg;
import com.seckill.miaosha.util.Md5Util;
import com.seckill.miaosha.util.UUIDUtill;
import com.seckill.miaosha.vo.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by syw on 2018/6/20.
 */

/** 秒杀 User Service 层
 * */
@Service
public class MiaoshaUserService {

    public static final  String COOKIE_TOKEN_NAME = "token";
    @Autowired
    private MiaoshaUserDao miaoshaUserDao;

    @Autowired
    private RedisService redisService;

    /** 将从数据库取对象 利用优化变为从缓存中取 对象数据
     *
     * @param id
     * @return
     */
    public MiaoshaUser getById(Long id){
        //取缓存
        MiaoshaUser user = redisService.get(MiaoshaUserKey.getByName,""+id,MiaoshaUser.class);
        if (user != null){//取到返回
            return user;
        }
        //取不到 从数据库里取 再放到 缓存里
        user =  miaoshaUserDao.getById(id);
        if (user !=null){
            redisService.set(MiaoshaUserKey.getByName,""+id,user);
        }
        return user;
    }

    /**更新用户密码方法： 涉及到对象缓存 ---若更新对象缓存的相关的数据 要处理缓存
     *  同步数据库和缓存的信息，不然会造成数据不一致的情况
     * */
    public boolean updatePassword(String token,long id,String formPassword){
        /*根据id 取出对应用户，更新对应数据*/
        MiaoshaUser user = getById(id);
        if (user == null){//如果没有该用户，说明 手机号不存在
            throw  new GlobalException(CodeMsg.LOGIN_ERROR_USER_NOT_ERROR);
        }
        // 更新数据库 信息
        MiaoshaUser updateUser = new MiaoshaUser();
        updateUser.setId(id);
        /*设置密码 到数据库 ，这时候 应该是formPassword ，更新密码一定是先在前端填入 密码，然后前端做 一次 加密传进来*/
        updateUser.setPassword(Md5Util.formPassToDBPass(formPassword,user.getSalt()));
        miaoshaUserDao.updatePassword(updateUser);
        // 更新完数据库信息，防止缓存中信息不一致，处理缓存 且涉及到所有该对象的缓存都需要处理
        // 一个 是 根据 token 获取对象，所以需要更新 token key 的缓存对象数据， 一个是根据id 获取对象，同理
        /** 处理缓存：
         *  1. 删除相关缓存数据
         *  2. 更新相关缓存中的数据
         * */
        redisService.delete(MiaoshaUserKey.getByName,""+id);//该对象缓存可以直接删，因为没有可以从数据取
        //但是token 缓存不能删除，而是应该修改重新设置，不然就无法登陆了(因为我们登陆是从缓存中取)
        user.setPassword(updateUser.getPassword());
        //将对象 携带新的密码放入缓存
        redisService.set(MiaoshaUserKey.token,token,user);
        return true;
    }

    /** 登陆验证方法
     * @param loginVo
     * @return
     */
    public boolean login(HttpServletResponse response, LoginVo loginVo) {
        /*判断 数据对象是否存在*/
        if (loginVo == null )
            throw  new GlobalException(CodeMsg.SERVER_ERROR);
        String mobile = loginVo.getMobile();
        String password = loginVo.getPassword();
        //判断手机号 是否能查到对象
        MiaoshaUser miaoshaUser = getById(Long.valueOf(mobile));//从缓存中取
        if (miaoshaUser == null){
            throw new GlobalException(CodeMsg.LOGIN_ERROR_USER_NOT_ERROR);
        }
        //若手机号存在,对象存在
        //验证密码
        /**通过 传入的密码 和 数据库获取的盐值 进行 MD5 拼接
         * 再和 数据中的密码比较 若相等的 登陆成功
         * */
        String dbPass = miaoshaUser.getPassword();
        String dbsalt = miaoshaUser.getSalt();
        String formPass = Md5Util.formPassToDBPass(password,dbsalt);
        if (!formPass.equals(dbPass)){
            throw new GlobalException(CodeMsg.LOGIN_ERROR_PASS_ERROR);
        }
        /*到达这里说明登陆成功
        * 需要保存 相关 session信息写入 cookie 用于登陆*/
        /*封装一个addCookie 方法 方便 重用*/
        String token = UUIDUtill.uuid();
        addCookie(miaoshaUser,token,response);//登陆成功 写入token ，同时写入 缓存和 cookie 之中
        return true;

    }

    /**
     *  根据Token 取出 对象信息
     * @param token
     * @return
     */
    public MiaoshaUser getByToken(HttpServletResponse response,String token) {
        if (StringUtils.isEmpty(token)){
            return null;
        }
        /*根据token 和前缀  去redis 中取出 对应的 用户信息值*/
        MiaoshaUser user =  redisService.get(MiaoshaUserKey.token,token,MiaoshaUser.class);
        /*延长有效期*/
        if (user!=null){
            addCookie(user,token,response);
        }
        return user;
    }

    private void addCookie(MiaoshaUser miaoshaUser,String token,HttpServletResponse response){
//        String token = UUIDUtill.uuid();  当我们延长有效期的时候 没有必要每次都生成新的token 直接延用 老的就可以了

        /*将用户sessionId和用户信息 以键值形式 保存到 redis 中*/
        redisService.set(MiaoshaUserKey.token,token,miaoshaUser);
        /*cookie 键值对都是 String类型*/
        Cookie cookie = new Cookie(COOKIE_TOKEN_NAME,token);
        /*设置cookie的过期时间*/
        cookie.setMaxAge(MiaoshaUserKey.token.expireSeconds());
        /*cookie 保存路径*/
        cookie.setPath("/");
        /*需要一个 response 对象 将cookie 返回给 客户端*/
        response.addCookie(cookie);
    }
}
