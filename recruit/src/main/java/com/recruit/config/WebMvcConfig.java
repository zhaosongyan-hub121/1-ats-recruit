package com.recruit.config;

import com.recruit.security.JwtInterceptor;
import com.recruit.security.PageAuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Spring MVC 配置，注册 JWT 拦截器和页面权限拦截器，配置拦截路径和排除路径
 */
@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final JwtInterceptor jwtInterceptor;
    private final PageAuthInterceptor pageAuthInterceptor;

    /**
     * 注册拦截器：JWT 拦截器拦截 REST API 路径并排除鉴权白名单；页面鉴权拦截器保护管理端页面路由
     *
     * @param registry 拦截器注册表
     */
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
                        "/api/companies/**",
                        "/api/companies/all",
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

    /**
     * 配置跨域策略，允许所有来源访问 /api/** 路径
     *
     * @param registry CORS 注册表
     */
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
