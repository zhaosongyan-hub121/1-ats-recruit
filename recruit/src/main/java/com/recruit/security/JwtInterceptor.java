package com.recruit.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.recruit.common.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    public static final String TOKEN_COOKIE = "ATS_TOKEN";

    private final JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String token = extractToken(request);
        if (token == null) {
            throw new BusinessException(401, "未登录或 token 缺失");
        }
        try {
            DecodedJWT jwt = jwtUtils.parse(token);
            Long userId = jwt.getClaim("userId").asLong();
            String username = jwt.getClaim("username").asString();
            String role = jwt.getClaim("role").asString();
            Long companyId = jwt.getClaim("companyId") != null ? jwt.getClaim("companyId").asLong() : null;
            UserContext.set(new UserContext.LoginUser(userId, username, role, companyId));
            return true;
        } catch (Exception e) {
            log.warn("token 校验失败: {}", e.getMessage());
            throw new BusinessException(401, "token 无效或已过期");
        }
    }

    public static String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        javax.servlet.http.Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (javax.servlet.http.Cookie c : cookies) {
                if (TOKEN_COOKIE.equals(c.getName())) {
                    return c.getValue();
                }
            }
        }
        return null;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();
    }
}