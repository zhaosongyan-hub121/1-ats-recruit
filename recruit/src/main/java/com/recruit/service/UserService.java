package com.recruit.service;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruit.common.BusinessException;
import com.recruit.entity.User;
import com.recruit.mapper.UserMapper;
import com.recruit.security.JwtUtils;
import com.recruit.dto.LoginRequest;
import com.recruit.dto.LoginResponse;
import com.recruit.dto.RegisterRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;

    public LoginResponse login(LoginRequest req) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, req.getUsername()));
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }
        if (!BCrypt.checkpw(req.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "密码错误");
        }
        String token = jwtUtils.generate(user.getId(), user.getUsername(), user.getRole());
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getRealName(), user.getRole(),
                user.getEmail(), user.getPhone(), user.getCompany(), user.getAvatar());
    }

    public LoginResponse register(RegisterRequest req) {
        if (!req.getPassword().equals(req.getConfirmPassword())) {
            throw new BusinessException(400, "两次密码输入不一致");
        }
        User exists = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, req.getUsername()));
        if (exists != null) {
            throw new BusinessException(400, "用户名已被注册");
        }
        if (StringUtils.hasText(req.getEmail())) {
            User emailExists = userMapper.selectOne(new LambdaQueryWrapper<User>()
                    .eq(User::getEmail, req.getEmail()));
            if (emailExists != null) {
                throw new BusinessException(400, "该邮箱已被注册");
            }
        }
        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(BCrypt.hashpw(req.getPassword(), BCrypt.gensalt()));
        user.setRealName(req.getRealName());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setRole("CANDIDATE");
        userMapper.insert(user);
        String token = jwtUtils.generate(user.getId(), user.getUsername(), user.getRole());
        return new LoginResponse(token, user.getId(), user.getUsername(), user.getRealName(), user.getRole(),
                user.getEmail(), user.getPhone(), user.getCompany(), user.getAvatar());
    }

    public Page<User> page(long current, long size, String keyword) {
        Page<User> page = new Page<>(current, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(User::getUsername, keyword)
                    .or().like(User::getRealName, keyword);
        }
        wrapper.orderByDesc(User::getCreatedAt);
        return userMapper.selectPage(page, wrapper);
    }

    public User get(Long id) {
        return userMapper.selectById(id);
    }

    public Long create(User user, String rawPassword) {
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("ADMIN");
        }
        if (!StringUtils.hasText(rawPassword)) {
            throw new BusinessException(400, "密码不能为空");
        }
        user.setPassword(BCrypt.hashpw(rawPassword, BCrypt.gensalt()));
        userMapper.insert(user);
        return user.getId();
    }

    public void update(User user) {
        User existing = userMapper.selectById(user.getId());
        if (existing == null) {
            throw new BusinessException(404, "用户不存在");
        }
        existing.setUsername(user.getUsername());
        existing.setRealName(user.getRealName());
        existing.setRole(user.getRole());
        userMapper.updateById(existing);
    }

    public void resetPassword(Long id, String newRawPassword) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        if (!StringUtils.hasText(newRawPassword)) {
            throw new BusinessException(400, "新密码不能为空");
        }
        user.setPassword(BCrypt.hashpw(newRawPassword, BCrypt.gensalt()));
        userMapper.updateById(user);
    }

    public void delete(Long id) {
        userMapper.deleteById(id);
    }
}
