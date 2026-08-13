package com.recruit.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 页面权限拦截器，控制 Thymeleaf 页面的访问权限。
 *
 * <p>与 {@link JwtInterceptor} 不同，本拦截器面向服务端渲染页面，
 * 校验失败时不抛异常而是通过 {@code sendRedirect} 跳转到登录页或提示页，
 * 提供更友好的浏览器端体验。
 *
 * <h3>技术亮点</h3>
 * <ul>
 *   <li>基于角色（RBAC）的页面访问控制，例如 {@code /dashboard}、{@code /page/} 前缀
 *       的管理后台页面仅 ADMIN / HR 可访问，CANDIDATE 将被重定向到候选人门户；</li>
 *   <li>复用 {@link JwtInterceptor#extractToken} 实现 Token 提取，避免逻辑重复；</li>
 *   <li>校验通过后同样将用户信息写入 {@link UserContext}，供 Thymeleaf 模板渲染时获取；</li>
 *   <li>请求结束自动清理 ThreadLocal，防止线程复用导致的用户串号。</li>
 * </ul>
 *
 * @see JwtInterceptor
 * @see UserContext
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PageAuthInterceptor implements HandlerInterceptor {

    private final JwtUtils jwtUtils;

    /**
     * 前置拦截：校验页面访问 Token 并执行基于角色的页面权限控制。
     *
     * <p>处理流程：
     * <ol>
     *   <li>提取 Token，缺失则 302 重定向到 {@code /login}；</li>
     *   <li>校验 Token 签名与过期，失败则重定向到 {@code /login}；</li>
     *   <li>解析角色，对 {@code /dashboard}、{@code /page/} 前缀的管理页面，
     *       CANDIDATE 角色重定向到 {@code /portal/profile?error=admin_only}；</li>
     *   <li>校验通过则将用户信息写入 {@link UserContext} 并放行。</li>
     * </ol>
     *
     * @param request  HTTP 请求
     * @param response HTTP 响应（用于重定向）
     * @param handler  目标处理器
     * @return {@code true} 放行；{@code false} 表示已通过重定向中断请求
     * @throws Exception 重定向或解析过程中发生的异常
     */
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

    /**
     * 请求完成后回调：清理 ThreadLocal 中存储的用户上下文。
     *
     * <p>无论页面渲染是否抛出异常都会执行，确保线程归还线程池后不残留用户信息，
     * 防止内存泄漏与用户串号问题。
     *
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @param handler  目标处理器
     * @param ex       业务处理过程中抛出的异常（可为 null）
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();
    }
}