package com.atguigu.lease.common.utils;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.util.Date;

public class JwtUtil {


    public static String CreateToken(Long userId, String password) {
        SecretKey secretKey = Keys.hmacShaKeyFor("MGRy8GUfKmub2aVHWA7HS6NQuUJHrDRg".getBytes());
        String jwt = Jwts.builder()
                .setExpiration(new Date(System.currentTimeMillis() + 3600000))
                .setSubject("LOGIN_USER")
                .claim("userId", userId)
                .claim("password", password)
                .signWith(secretKey, SignatureAlgorithm.HS256)
                .compact();
        return jwt;
    }

    public static void parseToke(){}


}
