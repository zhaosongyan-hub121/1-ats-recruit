package com.recruit.service;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.recruit.common.BusinessException;
import com.recruit.dto.LoginRequest;
import com.recruit.dto.LoginResponse;
import com.recruit.entity.User;
import com.recruit.mapper.UserMapper;
import com.recruit.security.JwtUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("用户认证服务单元测试")
class UserServiceTest {

    @Mock
    private UserMapper userMapper;
    @Mock
    private JwtUtils jwtUtils;

    @InjectMocks
    private UserService userService;

    private User user;
    private String rawPassword;

    @BeforeEach
    void setUp() {
        rawPassword = "admin123";
        user = new User();
        user.setId(1L);
        user.setUsername("admin");
        user.setPassword(BCrypt.hashpw(rawPassword, BCrypt.gensalt()));
        user.setRealName("系统管理员");
        user.setRole("ADMIN");
    }

    @Test
    @DisplayName("登录成功：返回Token及用户信息")
    void login_success() {
        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword(rawPassword);

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);
        when(jwtUtils.generate(1L, "admin", "ADMIN", null)).thenReturn("mock-jwt-token");

        LoginResponse resp = userService.login(req);

        assertNotNull(resp);
        assertEquals("mock-jwt-token", resp.getToken());
        assertEquals(1L, resp.getUserId());
        assertEquals("admin", resp.getUsername());
        assertEquals("系统管理员", resp.getRealName());
        assertEquals("ADMIN", resp.getRole());

        verify(userMapper, times(1)).selectOne(any(LambdaQueryWrapper.class));
        verify(jwtUtils, times(1)).generate(1L, "admin", "ADMIN", null);
    }

    @Test
    @DisplayName("登录失败：用户不存在抛出401")
    void login_userNotFound_throws401() {
        LoginRequest req = new LoginRequest();
        req.setUsername("nobody");
        req.setPassword("any");

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.login(req));
        assertEquals(401, ex.getCode());
        assertEquals("用户不存在", ex.getMessage());
        verify(jwtUtils, never()).generate(any(), any(), any(), any());
    }

    @Test
    @DisplayName("登录失败：密码错误抛出401")
    void login_wrongPassword_throws401() {
        LoginRequest req = new LoginRequest();
        req.setUsername("admin");
        req.setPassword("wrong-password");

        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(user);

        BusinessException ex = assertThrows(BusinessException.class, () -> userService.login(req));
        assertEquals(401, ex.getCode());
        assertEquals("密码错误", ex.getMessage());
        verify(jwtUtils, never()).generate(any(), any(), any(), any());
    }

    @Test
    @DisplayName("BCrypt校验：相同密码多次hash结果不同但校验通过")
    void bcrypt_samePassword_differentSalt() {
        String hash1 = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
        String hash2 = BCrypt.hashpw(rawPassword, BCrypt.gensalt());

        assertNotEquals(hash1, hash2);
        assertTrue(BCrypt.checkpw(rawPassword, hash1));
        assertTrue(BCrypt.checkpw(rawPassword, hash2));
    }
}
