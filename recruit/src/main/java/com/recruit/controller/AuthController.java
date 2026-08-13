package com.recruit.controller;

import com.auth0.jwt.interfaces.DecodedJWT;
import com.recruit.common.R;
import com.recruit.dto.LoginRequest;
import com.recruit.dto.LoginResponse;
import com.recruit.dto.RegisterRequest;
import com.recruit.entity.User;
import com.recruit.security.JwtInterceptor;
import com.recruit.security.JwtUtils;
import com.recruit.security.UserContext;
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

/**
 * 鉴权控制器
 * <p>
 * 处理用户登录、注册、登出和当前用户信息获取。
 * 登录成功后JWT Token同时通过Cookie和响应体返回，支持前后端双通道鉴权。
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "鉴权接口", description = "用户登录/注册与 JWT Token 获取（无需鉴权）")
public class AuthController {

    private final UserService userService;
    private final JwtUtils jwtUtils;

    /**
     * 用户登录
     * <p>
     * 校验账号密码及角色一致性，成功后返回JWT Token并写入Cookie。
     *
     * @param req      登录请求（用户名、密码、可选角色）
     * @param response HTTP响应，用于写入Token Cookie
     * @return 登录响应，包含Token和用户信息
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "输入用户名密码，返回 JWT Token。管理员账号：admin / admin123")
    public R<LoginResponse> login(
            @Parameter(description = "登录请求体", required = true)
            @RequestBody @Valid LoginRequest req,
            HttpServletResponse response) {
        LoginResponse data = userService.login(req);
        addTokenCookie(response, data.getToken());
        return R.ok(data);
    }

    /**
     * 用户注册
     * <p>
     * 支持求职者和HR角色注册，HR注册时自动创建并绑定企业信息。
     * 注册成功后自动登录返回Token。
     *
     * @param req      注册请求
     * @param response HTTP响应，用于写入Token Cookie
     * @return 登录响应
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "求职者注册账号，注册成功自动登录返回 Token")
    public R<LoginResponse> register(
            @RequestBody @Valid RegisterRequest req,
            HttpServletResponse response) {
        LoginResponse data = userService.register(req);
        addTokenCookie(response, data.getToken());
        return R.ok(data);
    }

    /**
     * 用户登出
     * <p>
     * 清除浏览器Cookie中的Token，前端需同时清除localStorage。
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "清除本地 Cookie，前端还需删除 localStorage 中的 token")
    public R<Void> logout(HttpServletRequest request, HttpServletResponse response) {
        Cookie c = new Cookie(JwtInterceptor.TOKEN_COOKIE, "");
        c.setPath("/");
        c.setMaxAge(0);
        response.addCookie(c);
        return R.ok();
    }

    /**
     * 获取当前登录用户信息
     * <p>
     * 从请求的Cookie或Authorization头中解析Token，返回当前用户信息。
     * 未登录或Token过期时返回data=null。
     */
    @GetMapping("/me")
    @Operation(summary = "获取当前登录用户信息", description = "从 Cookie/Token 中解析当前用户，未登录返回 data=null")
    public R<LoginResponse> me(HttpServletRequest request) {
        String token = JwtInterceptor.extractToken(request);
        if (token == null) {
            return R.ok(null);
        }
        try {
            DecodedJWT jwt = jwtUtils.parse(token);
            Long userId = jwt.getClaim("userId").asLong();
            User fullUser = userService.get(userId);
            if (fullUser == null) {
                return R.ok(null);
            }
            LoginResponse resp = new LoginResponse(null, fullUser.getId(), fullUser.getUsername(),
                    fullUser.getRealName(), fullUser.getRole(), fullUser.getEmail(), fullUser.getPhone(),
                    fullUser.getCompany(), fullUser.getAvatar());
            resp.setCompanyId(fullUser.getCompanyId());
            return R.ok(resp);
        } catch (Exception e) {
            return R.ok(null);
        }
    }

    private void addTokenCookie(HttpServletResponse response, String token) {
        Cookie c = new Cookie(JwtInterceptor.TOKEN_COOKIE, token);
        c.setPath("/");
        c.setHttpOnly(false);
        c.setMaxAge(7 * 24 * 3600);
        response.addCookie(c);
    }
}