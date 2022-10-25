package com.example.demo.controller;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class config implements WebMvcConfigurer {
    @Override
    public void addInterceptors(InterceptorRegistry registry){
        registry.addInterceptor(Interceptor()).addPathPatterns("/**")
                .excludePathPatterns("/gettoken")
                .excludePathPatterns("/")
                .excludePathPatterns("/login");
    }
    @Bean
    public Interceptor Interceptor(){
        return new Interceptor();
    }
}
