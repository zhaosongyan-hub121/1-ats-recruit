package com.recruit.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
            Long userId = jwt.getClaim("userId").asLong();
            String username = jwt.getClaim("username").asString();
            String role = jwt.getClaim("role").asString();
            Long companyId = jwt.getClaim("companyId") != null ? jwt.getClaim("companyId").asLong() : null;

            String requestURI = request.getRequestURI();

            if (requestURI.startsWith("/dashboard") || requestURI.startsWith("/page/")) {
                if ("CANDIDATE".equals(role)) {
                    response.sendRedirect("/portal/profile?error=admin_only");
                    return false;
                }
            }

            UserContext.set(new UserContext.LoginUser(userId, username, role, companyId));
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