package com.recruit.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * JWT 工具类，负责 Token 的生成、解析与验证。
 *
 * <p>Token 载荷中编码 userId、username、role、companyId 四项核心声明，
 * 配合 {@link JwtInterceptor} 实现无状态（Stateless）鉴权，无需在服务端存储会话。
 *
 * <h3>技术亮点</h3>
 * <ul>
 *   <li>采用 HMAC256 对称签名算法，性能优于非对称算法（RSA/ECDSA）；</li>
 *   <li>通过 {@code @Value} 注入密钥与过期时长，默认 8 小时，支持配置化；</li>
 *   <li>幂等支持含 / 不含 companyId 两种重载，兼容候选人与管理端用户；</li>
 *   <li>{@link #parse(String)} 内部完成签名校验与过期校验，校验失败抛出运行时异常。</li>
 * </ul>
 *
 * @see JwtInterceptor
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

    /**
     * 生成 JWT Token（不含企业 ID）。
     * <p>通常用于候选人侧用户，companyId 默认为 null。
     *
     * @param userId   用户 ID
     * @param username 用户名
     * @param role     角色标识（ADMIN / HR / CANDIDATE）
     * @return 已签名的 JWT 字符串
     */
    public String generate(Long userId, String username, String role) {
        return generate(userId, username, role, null);
    }

    /**
     * 生成 JWT Token（含企业 ID）。
     * <p>用于企业端用户（HR/ADMIN），将 companyId 写入声明以便业务层直接获取，
     * 避免二次查库。Token 包含签发时间（iat）与过期时间（exp）。
     *
     * @param userId    用户 ID
     * @param username  用户名
     * @param role      角色标识（ADMIN / HR / CANDIDATE）
     * @param companyId 所属企业 ID，为 null 时不写入该声明
     * @return 已签名的 JWT 字符串
     */
    public String generate(Long userId, String username, String role, Long companyId) {
        if (companyId != null) {
            return JWT.create()
                    .withSubject("recruit")
                    .withClaim("userId", userId)
                    .withClaim("username", username)
                    .withClaim("role", role)
                    .withClaim("companyId", companyId)
                    .withIssuedAt(new Date())
                    .withExpiresAt(new Date(System.currentTimeMillis() + expireMillis))
                    .sign(Algorithm.HMAC256(secret));
        }
        return JWT.create()
                .withSubject("recruit")
                .withClaim("userId", userId)
                .withClaim("username", username)
                .withClaim("role", role)
                .withIssuedAt(new Date())
                .withExpiresAt(new Date(System.currentTimeMillis() + expireMillis))
                .sign(Algorithm.HMAC256(secret));
    }

    /**
     * 解析并验证 Token。
     * <p>使用相同的 HMAC256 密钥构建校验器，对签名与过期时间进行验证。
     * 校验失败（签名错误 / 过期 / 格式非法）将抛出
     * {@code JWTVerificationException}，由调用方捕获并转换为业务异常。
     *
     * @param token 待校验的 JWT 字符串
     * @return 解码后的 JWT 对象，可通过 {@link DecodedJWT#getClaim(String)} 获取声明
     */
    public DecodedJWT parse(String token) {
        return JWT.require(Algorithm.HMAC256(secret))
                .build()
                .verify(token);
    }
}
