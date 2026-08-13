package com.recruit.security;

/**
 * 用户上下文，基于 {@link ThreadLocal} 存储当前请求的登录用户信息。
 *
 * <p>生命周期与单次 HTTP 请求绑定：在拦截器（{@code JwtInterceptor} /
 * {@code PageAuthInterceptor}）中通过 {@link #set(LoginUser)} 写入，
 * 在业务层（Service / Controller）通过 {@link #get()} 无参获取，
 * 请求结束时由拦截器的 {@code afterCompletion} 调用 {@link #clear()} 清理。
 *
 * <h3>技术亮点</h3>
 * <ul>
 *   <li>以 ThreadLocal 实现请求级隔离，避免在方法签名中层层传递用户对象；</li>
 *   <li>配合线程池使用时必须显式 {@link #clear()}，否则会引发内存泄漏与用户串号，
 *       本上下文由拦截器统一兜底清理；</li>
 *   <li>内置角色判断方法（{@link LoginUser#isAdmin()} 等），简化业务层权限校验代码。</li>
 * </ul>
 *
 * @see UserContext.LoginUser
 */
public class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    /**
     * 设置当前线程的登录用户。
     * <p>由认证拦截器在校验 Token 通过后调用。
     *
     * @param user 登录用户信息，可为 null（表示清空）
     */
    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    /**
     * 获取当前线程的登录用户。
     *
     * @return 当前请求的登录用户；未登录或未设置时返回 {@code null}
     */
    public static LoginUser get() {
        return HOLDER.get();
    }

    /**
     * 清理当前线程的登录用户。
     * <p>必须由拦截器在请求结束时调用，防止线程复用导致的内存泄漏与用户串号。
     */
    public static void clear() {
        HOLDER.remove();
    }

    /**
     * 登录用户信息 DTO，封装从 JWT 解码出的核心用户声明。
     *
     * <p>包含 userId、username、role、companyId 四项核心字段，
     * 并提供 {@link #isAdmin()}、{@link #isHr()}、{@link #isCandidate()} 等角色判断便捷方法，
     * 供业务层在权限校验时直接调用，避免散落的字符串比较。
     */
    public static class LoginUser {
        private final Long userId;
        private final String username;
        private final String role;
        private final Long companyId;

        /**
         * 构造登录用户（不含企业 ID）。
         * <p>通常用于候选人侧用户。
         *
         * @param userId   用户 ID
         * @param username 用户名
         * @param role     角色标识
         */
        public LoginUser(Long userId, String username, String role) {
            this(userId, username, role, null);
        }

        /**
         * 构造登录用户（含企业 ID）。
         *
         * @param userId    用户 ID
         * @param username  用户名
         * @param role      角色标识（ADMIN / HR / CANDIDATE）
         * @param companyId 所属企业 ID，可为 null
         */
        public LoginUser(Long userId, String username, String role, Long companyId) {
            this.userId = userId;
            this.username = username;
            this.role = role;
            this.companyId = companyId;
        }

        public Long getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }

        public String getRole() {
            return role;
        }

        public Long getCompanyId() {
            return companyId;
        }

        /**
         * 判断当前用户是否为管理员（ADMIN）。
         *
         * @return 角色为 ADMIN 时返回 {@code true}
         */
        public boolean isAdmin() {
            return "ADMIN".equals(role);
        }

        /**
         * 判断当前用户是否为 HR。
         *
         * @return 角色为 HR 时返回 {@code true}
         */
        public boolean isHr() {
            return "HR".equals(role);
        }

        /**
         * 判断当前用户是否为候选人（CANDIDATE）。
         *
         * @return 角色为 CANDIDATE 时返回 {@code true}
         */
        public boolean isCandidate() {
            return "CANDIDATE".equals(role);
        }
    }
}