package com.tagging.utils;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTDecodeException;
import com.tagging.dto.user.TokenRsp;
import com.tagging.entity.User;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;

import java.security.PublicKey;
import java.util.Date;


public class TokenUtil {
    //设置过期时间为1个小时
    public static TokenRsp getToken(User user, long EXPIRE_TIME, long REFRESH_TIME){
        String token="";
        String encodedToken="";
        //过期时间
        Date expires = new Date();
        //刷新时间
        Date refresh =new Date();
        expires.setTime(System.currentTimeMillis() + EXPIRE_TIME);
        refresh.setTime(System.currentTimeMillis() + REFRESH_TIME);
        token= JWT.create().withAudience(String.valueOf(user.getUserId()))
                .withExpiresAt(expires)
                .sign(Algorithm.HMAC256(user.getUserPassword()));




        TokenRsp tokenRsp = new TokenRsp();
        tokenRsp.setToken(token);
        tokenRsp.setRefresh(refresh);
        tokenRsp.setExpires(expires);
        return tokenRsp;
    }
    //md5加密
    public static String md5(String src,String salt){
        String DbPassword = salt.charAt(2)+salt.charAt(3)+salt.charAt(5)+src+salt.charAt(1)+salt.charAt(4)+salt.charAt(0);
        return DigestUtils.md5Hex(DbPassword);
    }

}
