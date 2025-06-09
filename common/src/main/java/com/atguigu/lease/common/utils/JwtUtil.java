package com.atguigu.lease.common.utils;

import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.ResultCodeEnum;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtil {
    static SecretKey secretKey = Keys.hmacShaKeyFor("MGRy8GUfKmub2aVHWA7HS6NQuUJHrDRg".getBytes());

    public static String CreateToken(Long userId, String password) {

        String jwt = Jwts.builder()
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .setSubject("LOGIN_USER")
                .claim("userId", userId)
                .claim("password", password)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
        return jwt;
    }

    public static void parseToke(String token){
        if(token == null){
            throw new LeaseException(ResultCodeEnum.TOKEN_INVALID);
        }

        try{
            JwtParser jwtParser = Jwts.parserBuilder()
                    .setSigningKey(secretKey)
                    .build();
            jwtParser.parseClaimsJws(token);
        }catch (ExpiredJwtException e){
            throw new LeaseException(ResultCodeEnum.TOKEN_EXPIRED);
        }catch (JwtException e){
            throw new LeaseException(ResultCodeEnum.TOKEN_INVALID);
        }
    }


}
