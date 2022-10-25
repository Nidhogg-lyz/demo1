package com.example.demo.controller;


import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.JWTVerificationException;

import java.util.Date;
import java.util.Map;


public class tokenGenerator {
    private static int EXPIRE_TIME=600000;
    private static String secret="jwt_secret";//可以为用户密码
    public static String sign(String id, Map<String,Object> info){
        try{
            Date date = new Date(System.currentTimeMillis() + EXPIRE_TIME);
            Algorithm algorithm = Algorithm.HMAC256(secret);
            return JWT.create()
                    //将userId保存到token里面
                    .withAudience(id)
//                    //存放自定义数据
                    .withClaim("name", info.get("name").toString())
                    .withClaim("code",info.get("invitation_code").toString())
                    .withClaim("stu_no",info.get("student_no").toString())
                    .withClaim("gender",(Boolean)info.get("gender"))
//                    //10分钟后token过期
                    .withExpiresAt(date)
//                    //token的密钥
                    .sign(algorithm);
        }
        catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
    public static boolean check(String token){
        try {
            Algorithm algorithm  = Algorithm.HMAC256(secret);
            String id=getId(token);
            String name=getName(token);
            JWTVerifier verifier = JWT
                    .require(algorithm)
                    .withAudience(id)
                    .withClaim("name",name)
                    .build();
            verifier.verify(token);
            return true;
        }catch (JWTVerificationException e) {
            throw new RuntimeException("token 无效，请重新获取");
        }
    }
    public static String getId(String token){
        try {
            String userId = JWT.decode(token).getAudience().get(0);
            return userId;
        }catch (JWTDecodeException e) {
            return null;
        }
    }
    public static String getName(String token) {
        try {
            return JWT.decode(token).getClaim("name").asString();
        }catch (JWTDecodeException e) {
            return null;
        }
    }
    public static String getCode(String token){
        try {
            return JWT.decode(token).getClaim("code").asString();
        }catch (JWTDecodeException e) {
            return null;
        }
    }
    public static String getStu_no(String token){
        try {
            return JWT.decode(token).getClaim("stu_no").asString();
        }catch (JWTDecodeException e) {
            return null;
        }
    }
    public static Boolean getGender(String token){
        try {
            return JWT.decode(token).getClaim("gender").asBoolean();
        }catch (JWTDecodeException e) {
            return null;
        }
    }
}
