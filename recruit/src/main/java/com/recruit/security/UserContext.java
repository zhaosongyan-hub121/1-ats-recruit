package com.recruit.security;

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

    public static class LoginUser {
        private final Long userId;
        private final String username;
        private final String role;
        private final Long companyId;

        public LoginUser(Long userId, String username, String role) {
            this(userId, username, role, null);
        }

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

        public boolean isAdmin() {
            return "ADMIN".equals(role);
        }

        public boolean isHr() {
            return "HR".equals(role);
        }

        public boolean isCandidate() {
            return "CANDIDATE".equals(role);
        }
    }
}