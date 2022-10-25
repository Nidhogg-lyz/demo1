package com.example.demo.controller;

import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Method;

public class Interceptor implements HandlerInterceptor {
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object object) throws Exception{
        if (!(object instanceof HandlerMethod))
            return true;
        String token=request.getHeader("token");
        System.out.println("token: "+token);
        if(token==null){
            //response.sendError(401,"未提供token, 您尚未登录!"); //为什么会响应两次？ 即该句执行后，客户端还会有一个请求过来被拦截，表现为上述打印语句被执行了两次
            //response.addHeader("message","no token found, you've not login!"); // info in header
            response.getWriter().write("no token found, you've not login!"); // info in body
            return false;
//            throw new Exception("token无效!");
        }
        try{
            tokenGenerator.check(token);
        }
        catch (Exception e){
            //response.sendError(401,"token无效!\n"+e.toString());
            //response.addHeader("message","invalid token!\n"+e.toString());
            response.getWriter().write("invalid token!\n"+e.toString());
            return false;
        }
        System.out.println("token有效!: "+token);
        System.out.println("登录者id: "+tokenGenerator.getId(token));
        System.out.println("登录者name: "+tokenGenerator.getName(token));
        System.out.println("登录时间: "+System.currentTimeMillis());
        return true;
    }
}
