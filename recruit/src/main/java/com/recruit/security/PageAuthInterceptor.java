package com.recruit.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 页面路由鉴权拦截器（仅用于 Thymeleaf 页面渲染）
 *
 * 没有有效 token 时 302 重定向到 /login
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PageAuthInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = JwtInterceptor.extractToken(request);
        if (token == null) {
            response.sendRedirect("/login");
            return false;
        }
        try {
            DecodedJWT jwt = jwtUtils.parse(token);
            UserContext.set(new UserContext.LoginUser(
                    jwt.getClaim("userId").asLong(),
                    jwt.getClaim("username").asString(),
                    jwt.getClaim("role").asString()));
            return true;
        } catch (Exception e) {
            log.warn("页面访问 token 校验失败: {}", e.getMessage());
            response.sendRedirect("/login");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();
    }
}
