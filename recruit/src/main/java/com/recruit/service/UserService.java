package com.recruit.service;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruit.common.BusinessException;
import com.recruit.dto.LoginRequest;
import com.recruit.dto.LoginResponse;
import com.recruit.dto.RegisterRequest;
import com.recruit.entity.Company;
import com.recruit.entity.User;
import com.recruit.mapper.CompanyMapper;
import com.recruit.mapper.UserMapper;
import com.recruit.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 用户服务
 * <p>
 * 负责用户注册、登录、账号管理等核心业务。
 * 登录时校验账号密码及角色一致性，注册时支持求职者和HR两种角色，
 * HR注册自动创建并绑定企业信息。
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final JwtUtils jwtUtils;
    private final CompanyMapper companyMapper;

    /**
     * 用户登录
     * <p>
     * 校验账号密码，若前端选择了角色则额外校验角色一致性，
     * 通过后生成JWT令牌返回用户信息。
     *
     * @param req 登录请求，包含用户名、密码和可选的角色
     * @return 登录响应，包含JWT令牌和用户信息
     * @throws BusinessException 401 用户不存在/密码错误/角色不匹配
     */
    public LoginResponse login(LoginRequest req) {
        User user = userMapper.selectOne(new LambdaQueryWrapper<User>()
                .eq(User::getUsername, req.getUsername()));
        if (user == null) {
            throw new BusinessException(401, "用户不存在");
        }
        if (!BCrypt.checkpw(req.getPassword(), user.getPassword())) {
            throw new BusinessException(401, "密码错误");
        }
        if (req.getRole() != null && !req.getRole().isEmpty()) {
            if (!req.getRole().equals(user.getRole())) {
                throw new BusinessException(401, "角色与账号不匹配，请选择正确的登录角色");
            }
        }
        String token = jwtUtils.generate(user.getId(), user.getUsername(), user.getRole(), user.getCompanyId());
        LoginResponse response = new LoginResponse(token, user.getId(), user.getUsername(), user.getRealName(),
                user.getRole(), user.getEmail(), user.getPhone(), user.getCompany(), user.getAvatar());
        response.setCompanyId(user.getCompanyId());
        return response;
    }

    /**
     * 用户注册
     * <p>
     * 支持求职者和HR两种角色注册。HR注册时自动创建并绑定企业信息，
     * 若企业已存在则直接关联。注册成功后自动登录返回JWT。
     *
     * @param req 注册请求，包含用户信息和可选的企业信息(HR角色)
     * @return 登录响应
     * @throws BusinessException 400 密码不一致/用户名已存在/邮箱已注册
     */
    @Transactional
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

        String role = req.getRole() != null ? req.getRole() : "CANDIDATE";

        Company company = null;
        if ("HR".equals(role) && StringUtils.hasText(req.getCompanyName())) {
            company = companyMapper.selectOne(new LambdaQueryWrapper<Company>()
                    .eq(Company::getName, req.getCompanyName()));
            if (company == null) {
                company = new Company();
                company.setName(req.getCompanyName());
                company.setIndustry(req.getIndustry());
                company.setDescription(req.getCompanyDescription());
                if (StringUtils.hasText(req.getCompanySize())) {
                    company.setSize(req.getCompanySize());
                }
                company.setLogo(req.getCompanyName().length() > 2 ? req.getCompanyName().substring(0, 2) : req.getCompanyName());
                company.setLogoColor("#1677ff");
                company.setLocation("待补充");
                companyMapper.insert(company);
            }
        }

        User user = new User();
        user.setUsername(req.getUsername());
        user.setPassword(BCrypt.hashpw(req.getPassword(), BCrypt.gensalt()));
        user.setRealName(req.getRealName());
        user.setEmail(req.getEmail());
        user.setPhone(req.getPhone());
        user.setRole(role);
        if (company != null) {
            user.setCompany(company.getName());
            user.setCompanyId(company.getId());
        }
        userMapper.insert(user);

        String token = jwtUtils.generate(user.getId(), user.getUsername(), user.getRole(), user.getCompanyId());
        LoginResponse response = new LoginResponse(token, user.getId(), user.getUsername(), user.getRealName(),
                user.getRole(), user.getEmail(), user.getPhone(), user.getCompany(), user.getAvatar());
        response.setCompanyId(user.getCompanyId());
        return response;
    }

    /**
     * 分页查询用户列表
     *
     * @param current 页码
     * @param size    每页条数
     * @param keyword 搜索关键词（匹配用户名/真实姓名）
     * @return 分页用户数据
     */
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

    /**
     * 根据ID查询用户
     */
    public User get(Long id) {
        return userMapper.selectById(id);
    }

    /**
     * 创建用户（管理员操作）
     */
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

    /**
     * 更新用户信息（管理员操作）
     */
    public void update(User user) {
        User existing = userMapper.selectById(user.getId());
        if (existing == null) {
            throw new BusinessException(404, "用户不存在");
        }
        existing.setUsername(user.getUsername());
        existing.setRealName(user.getRealName());
        existing.setRole(user.getRole());
        existing.setCompany(user.getCompany());
        existing.setCompanyId(user.getCompanyId());
        userMapper.updateById(existing);
    }

    /**
     * 重置用户密码
     */
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

    /**
     * 删除用户（逻辑删除）
     */
    public void delete(Long id) {
        userMapper.deleteById(id);
    }
}