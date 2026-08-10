package com.recruit.security;

/**
 * 当前登录用户上下文（ThreadLocal）
 *
 * 拦截器校验通过后写入；请求结束在 finally 中清理。
 */
public class UserContext {

    private static final ThreadLocal<LoginUser> HOLDER = new ThreadLocal<>();

    public static void set(LoginUser user) {
        HOLDER.set(user);
    }

    public static LoginUser get() {
        return HOLDER.get();
    }

    public static void clear() {
        HOLDER.remove();
    }

    /** 当前登录用户简要信息 */
    public static class LoginUser {
        private final Long userId;
        private final String username;
        private final String role;

        public LoginUser(Long userId, String username, String role) {
            this.userId = userId;
            this.username = username;
            this.role = role;
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
    }
}
