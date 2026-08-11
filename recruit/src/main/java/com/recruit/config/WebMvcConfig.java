package com.recruit.config;

import com.recruit.security.JwtInterceptor;
import com.recruit.security.PageAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web 配置：JWT 拦截器（REST API） + 页面鉴权拦截器（Thymeleaf） + CORS
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;
    private final PageAuthInterceptor pageAuthInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 1) REST API 鉴权：除了白名单路径外，全部拦截
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        // 鉴权接口：登录/注册/登出/me
                        "/api/auth/login",
                        "/api/auth/register",
                        "/api/auth/logout",
                        "/api/auth/me",
                        // 求职者端浏览类接口（匿名可访问）
                        "/api/portal/positions/**",
                        "/api/portal/positions",
                        "/api/portal/stats",
                        "/api/portal/categories/**",
                        "/api/portal/locations/**",
                        // Swagger / OpenAPI
                        "/api-docs/**",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/webjars/**"
                );

        // 2) 页面路由鉴权：管理端页面必须先登录
        registry.addInterceptor(pageAuthInterceptor)
                .addPathPatterns("/dashboard", "/page/**")
                .excludePathPatterns();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
