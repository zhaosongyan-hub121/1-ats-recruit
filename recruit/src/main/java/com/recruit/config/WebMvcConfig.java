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
        // 1) REST API 鉴权：除登录接口、Swagger 文档外全部拦截
        registry.addInterceptor(jwtInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                        "/api/auth/login",
                        "/api/portal/**",
                        // Swagger / OpenAPI
                        "/api-docs/**",
                        "/swagger-ui.html",
                        "/swagger-ui/**",
                        "/webjars/**"
                );

        // 2) 页面路由鉴权：进看板及子页面前必须先登录，否则 302 跳 /login
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
