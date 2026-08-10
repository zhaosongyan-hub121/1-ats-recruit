package com.recruit.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * JWT 工具：生成 / 解析 / 校验 token
 *
 * token 载荷：userId、username、role
 */
@Component
public class JwtUtils {

    private final String secret;
    private final long expireMillis;

    public JwtUtils(@Value("${jwt.secret}") String secret,
                    @Value("${jwt.expire-hours:8}") long expireHours) {
        this.secret = secret;
        this.expireMillis = expireHours * 3600_000L;
    }

    /** 生成 token */
    public String generate(Long userId, String username, String role) {
        return JWT.create()
                .withSubject("recruit")
                .withClaim("userId", userId)
                .withClaim("username", username)
                .withClaim("role", role)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + expireMillis))
                .sign(Algorithm.HMAC256(secret));
    }

    /** 解析 token（校验失败抛异常） */
    public DecodedJWT parse(String token) {
        return JWT.require(Algorithm.HMAC256(secret))
                .build()
                .verify(token);
    }
}
