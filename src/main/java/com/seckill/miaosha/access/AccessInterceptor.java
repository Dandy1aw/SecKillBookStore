package com.seckill.miaosha.access;

import com.alibaba.fastjson.JSON;
import com.seckill.miaosha.domain.MiaoshaUser;
import com.seckill.miaosha.redis.MiaoshaKey;
import com.seckill.miaosha.redis.RedisService;
import com.seckill.miaosha.result.CodeMsg;
import com.seckill.miaosha.result.Result;
import com.seckill.miaosha.service.MiaoshaUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;

/**用于实现 注解的 拦截器 需要实现HandlerInteceptorAdapter
 * */
@Service
public class AccessInterceptor extends HandlerInterceptorAdapter {

    @Autowired
    MiaoshaUserService miaoshaUserService;

    @Autowired
    RedisService redisService;
    /*改写这个方法，表示在方法执行之前拦截*/
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod) {//如果是HandlerMethod 类，强转，拿到注解
            /*拿到用户*/
            MiaoshaUser user = getUser(request,response);
            /*为了方便实现user拦截器,存入当前user对象，这里直接就可以直接结合 登陆功能 做了*/
            UserContext.setUser(user);
            HandlerMethod hm = (HandlerMethod)handler;
            AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);
            if (accessLimit == null){
                return true;//没有注解 就放行表示执行完成
            }
            int maxCount = accessLimit.maxCount();//获取方法上注解的参数
            int seconds = accessLimit.seconds();
            boolean needLogin = accessLimit.needLogin();//判断登录 这里需要拿到用户User
            /**由于之前只是通过拦截器 获取方法上的User变量，这里做一个拦截器来 判断用户是否登录
             * 用到之前 UserArguementResolver中获得用户的代码
             * */
            String urlKey = request.getRequestURI();
            /*第一部： 登陆验证*/
            if (needLogin){//如果注解中 表示需要登录
                if (user==null){//但是查不到用户
                    render(response,CodeMsg.SERVER_ERROR);//将错误码写入输出流输出出去
                    return false;//拦截 该方法，拦截器中只能 返回 true or false
                }
                //需要登录的拼上 用户id 来区别
                urlKey+="_"+user.getId();
            }else {
                //do nothing! //不登录的就不拼
            }
            //第三部：访问时限设计,即定义缓存的生效时间 传入一个时间，获得一个有时间限制的前缀对象
            MiaoshaKey ky = MiaoshaKey.withExpire(seconds);
            //第二步：计数 限流逻辑
            Integer count = redisService.get(ky,urlKey,Integer.class);
            if (count == null){
                redisService.set(ky,urlKey,1);//如果没有，说明没访问过，置1
            }else if (count <maxCount){//设置 如果小于我们 的防刷次数
                redisService.incr(ky,urlKey);//小于5 就+1
            }else {//说明大于最大次数
                render(response,CodeMsg.REQUEST_OVER_LIMIT);
                return false;
            }
            return true;
        }




        return super.preHandle(request, response, handler);
    }

    /**render 方法为了 拦截的时候 输出到 浏览器，获得 response
     * */
    private void render(HttpServletResponse response, CodeMsg serverError) throws IOException {
/*注意 这里 输出的是 json 数据，所以 务必要定义 contentType 以及编码*/
        response.setContentType("application/json;charset=utf-8");
        OutputStream out = response.getOutputStream();
        String str = JSON.toJSONString(Result.error(serverError));//转化为Json传输出
        out.write(str.getBytes("UTF-8"));
        out.flush();
        out.close();
    }


    /**借用 获得用户的代码
     * */
    private MiaoshaUser getUser(HttpServletRequest request, HttpServletResponse response){
        String paramToken = request.getParameter(MiaoshaUserService.COOKIE_TOKEN_NAME);
        String cookieToken = getCookieValue(request,MiaoshaUserService.COOKIE_TOKEN_NAME);

        if (StringUtils.isEmpty(cookieToken) && StringUtils.isEmpty(paramToken))
        {/*如果 cookie 中都没有值 返回 null 此时返回的 值 是给 MiaoshaUser 对象的 就是解析的参数值*/
            return null;
        }
        /*有限从paramToken 中取出 cookie值 若没有从 cookieToken 中取*/
        String token = StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
        return miaoshaUserService.getByToken(response,token);/*拿到 user 对象*/

    }
    private String getCookieValue(HttpServletRequest request, String cookieTokenName) {
        /*在 请求中 遍历所有的cookie 从中取到 我们需要的那一个cookie 就可以的*/
        Cookie[] cookies =  request.getCookies();
        /*请求中没有cookies 的时候返回null ?? 没有cookie ? 没有登录吗？*/
        if (cookies == null || cookies.length ==0)
        {
            return null;
        }
        for (Cookie cookie: cookies) {
            if (cookie.getName().equals(cookieTokenName))
                return cookie.getValue();
        }
        return null;
    }
}
