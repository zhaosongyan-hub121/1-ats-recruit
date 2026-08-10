package com.recruit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruit.common.R;
import com.recruit.entity.User;
import com.recruit.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "用户管理", description = "系统用户的增删改查、分页搜索与密码重置")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "分页查询用户", description = "支持关键词模糊搜索（用户名/姓名），按创建时间倒序")
    public R<Page<User>> page(
            @Parameter(description = "页码，从1开始") @RequestParam(defaultValue = "1") long current,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") long size,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {
        return R.ok(userService.page(current, size, keyword));
    }

    @GetMapping("/{id}")
    @Operation(summary = "用户详情")
    public R<User> get(@Parameter(description = "用户ID") @PathVariable Long id) {
        return R.ok(userService.get(id));
    }

    @PostMapping
    @Operation(summary = "新建用户", description = "body 中 password 为明文，服务端自动 BCrypt 哈希；role 为空默认 ADMIN")
    public R<Long> create(@RequestBody Map<String, Object> body) {
        User user = new User();
        user.setUsername((String) body.get("username"));
        user.setRealName((String) body.get("realName"));
        user.setRole((String) body.get("role"));
        String rawPassword = (String) body.get("password");
        return R.ok(userService.create(user, rawPassword));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新用户", description = "只更新 username、realName、role，不改密码")
    public R<Void> update(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @RequestBody User user) {
        user.setId(id);
        userService.update(user);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除用户（逻辑删除）")
    public R<Void> delete(@Parameter(description = "用户ID") @PathVariable Long id) {
        userService.delete(id);
        return R.ok();
    }

    @PostMapping("/{id}/reset-password")
    @Operation(summary = "重置用户密码", description = "body {password: 明文新密码}，服务端自动 BCrypt 哈希")
    public R<Void> resetPassword(
            @Parameter(description = "用户ID") @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String newPassword = body.get("password");
        userService.resetPassword(id, newPassword);
        return R.ok();
    }
}
