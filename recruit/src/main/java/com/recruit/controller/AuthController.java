package com.recruit.controller;

import com.recruit.common.R;
import com.recruit.dto.LoginRequest;
import com.recruit.dto.LoginResponse;
import com.recruit.security.JwtInterceptor;
import com.recruit.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "鉴权接口", description = "用户登录与 JWT Token 获取（无需鉴权）")
public class AuthController {

    private final UserService userService;

    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "输入用户名密码，返回 JWT Token。演示账号：admin / admin123")
    public R<LoginResponse> login(
            @Parameter(description = "登录请求体", required = true)
            @RequestBody @Valid LoginRequest req,
            HttpServletResponse response) {
        LoginResponse data = userService.login(req);
        Cookie c = new Cookie(JwtInterceptor.TOKEN_COOKIE, data.getToken());
        c.setPath("/");
        c.setHttpOnly(false);
        c.setMaxAge(7 * 24 * 3600);
        response.addCookie(c);
        return R.ok(data);
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "清除本地 Cookie，前端还需删除 localStorage 中的 token")
    public R<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie c = new Cookie(JwtInterceptor.TOKEN_COOKIE, "");
        c.setPath("/");
        c.setMaxAge(0);
        response.addCookie(c);
        return R.ok();
    }
}
