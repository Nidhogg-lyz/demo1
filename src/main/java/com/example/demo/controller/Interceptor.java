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
            response.sendError(-1,"未提供token, 您尚未登录!");
            return false;
//            throw new Exception("token无效!");
        }
        try{
            tokenGenerator.check(token);
        }
        catch (Exception e){
            response.sendError(-1,"token无效!\n"+e.toString());
            return false;
        }
        System.out.println("token有效!: "+token);
        System.out.println("登录者id: "+tokenGenerator.getId(token));
        System.out.println("登录者name: "+tokenGenerator.getName(token));
        System.out.println("登录时间: "+System.currentTimeMillis());
        return true;
    }
}
