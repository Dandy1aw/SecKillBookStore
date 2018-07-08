package com.seckill.miaosha.config;

import com.seckill.miaosha.domain.MiaoshaUser;
import com.seckill.miaosha.service.MiaoshaUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by syw on 2018/6/25.
 */
/*要将 MiaoshaUser 类 参数对象 注册 进来*/
@Service
public class UserArguementResolver implements HandlerMethodArgumentResolver {

    @Autowired
    private MiaoshaUserService miaoshaUserService;
    /* 判断是否是MiaoshaUser 类 */
    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        Class<?> clazz = methodParameter.getParameterType();/*获取参数类型*/
        return clazz == MiaoshaUser.class; /* 若是 MiaoshaUser 类 才进行下一步*/
    }
    /*具体操作  获取到user 对象的参数 值 */
    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer,
                                  NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {

        /*第一步 ： 取出request ,response */
        HttpServletRequest request = nativeWebRequest.getNativeRequest(HttpServletRequest.class);
        HttpServletResponse response = nativeWebRequest.getNativeResponse(HttpServletResponse.class);
        /*第二步 : 取出token */
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
