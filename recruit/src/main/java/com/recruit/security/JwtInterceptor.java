package com.recruit.security;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.recruit.common.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * JWT 认证拦截器，拦截受保护的 API 请求并验证 Token 有效性。
 *
 * <p>校验通过后，将解码出的用户信息（userId、username、role、companyId）
 * 封装为 {@link UserContext.LoginUser} 存入 {@link UserContext} 的 ThreadLocal 上下文，
 * 供后续业务层在整条调用链中无参获取当前登录用户。
 *
 * <h3>技术亮点</h3>
 * <ul>
 *   <li>双通道 Token 提取：优先读取 {@code Authorization: Bearer xxx} 请求头，
 *       回退到名为 {@link #TOKEN_COOKIE} 的 Cookie，兼容前后端分离与 SSR 场景；</li>
 *   <li>无状态鉴权：服务端不存储会话，水平扩展友好；</li>
 *   <li>统一异常出口：Token 缺失或校验失败均抛出 {@link BusinessException}（HTTP 401），
 *       由全局异常处理器转换为标准响应；</li>
 *   <li>请求结束自动清理 ThreadLocal，规避线程池场景下的内存泄漏与用户串号。</li>
 * </ul>
 *
 * @see JwtUtils
 * @see UserContext
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtInterceptor implements HandlerInterceptor {

    /** 承载 JWT 的 Cookie 名称，用于浏览器端 Token 持久化。 */
    public static final String TOKEN_COOKIE = "ATS_TOKEN";

    private final JwtUtils jwtUtils;

    /**
     * 前置拦截：提取并校验 Token，校验通过则将用户信息写入 ThreadLocal。
     *
     * <p>处理流程：
     * <ol>
     *   <li>通过 {@link #extractToken(HttpServletRequest)} 获取 Token，缺失则抛 401；</li>
     *   <li>调用 {@link JwtUtils#parse(String)} 校验签名与过期时间；</li>
     *   <li>解析 userId/username/role/companyId 声明，写入 {@link UserContext}；</li>
     *   <li>校验异常统一捕获并转换为 401 业务异常。</li>
     * </ol>
     *
     * @param request  HTTP 请求
     * @param response HTTP 响应
     * @param handler  目标处理器
     * @return {@code true} 放行；校验失败时通过抛出异常中断
     * @throws BusinessException Token 缺失或无效时抛出（HTTP 401）
     */
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

    /**
     * 从请求中提取 JWT Token，支持双通道提取。
     *
     * <p>提取顺序：
     * <ol>
     *   <li>优先解析 {@code Authorization} 请求头，匹配 {@code "Bearer "} 前缀后截取 Token；</li>
     *   <li>请求头缺失时，遍历 Cookie 查找名为 {@link #TOKEN_COOKIE} 的项。</li>
     * </ol>
     *
     * @param request HTTP 请求
     * @return Token 字符串；不存在时返回 {@code null}
     */
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

    /**
     * 请求完成后回调：清理 ThreadLocal 中存储的用户上下文。
     *
     * <p>无论业务处理是否抛出异常都会执行，确保线程归还线程池后不残留上一个请求的用户信息，
     * 防止内存泄漏与用户串号问题。
     *
     * @param request     HTTP 请求
     * @param response    HTTP 响应
     * @param handler     目标处理器
     * @param ex          业务处理过程中抛出的异常（可为 null）
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        UserContext.clear();
    }
}