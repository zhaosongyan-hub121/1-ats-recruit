package com.recruit.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruit.entity.Company;
import com.recruit.entity.Position;
import com.recruit.entity.User;
import com.recruit.mapper.CompanyMapper;
import com.recruit.mapper.PositionMapper;
import com.recruit.mapper.UserMapper;
import com.recruit.security.UserContext;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

/**
 * 岗位服务
 * <p>
 * 负责招聘岗位的CRUD操作，支持管理员全量查询和HR按公司隔离查询。
 * 集成Redis缓存（可选），支持自动降级，无Redis时直接查库。
 */
@Service
@RequiredArgsConstructor
public class PositionService {

    private static final String CACHE_KEY_PREFIX = "ats:pos:";
    private static final long CACHE_TTL_SEC = 30 * 60L;

    private final PositionMapper positionMapper;
    private final UserMapper userMapper;
    private final CompanyMapper companyMapper;

    /** Redis缓存服务，可选依赖，不可用时自动降级 */
    @Autowired(required = false)
    private CacheService cacheService;

    /**
     * 分页查询岗位列表（管理员视图，查看全部岗位）
     *
     * @param current 页码
     * @param size    每页条数
     * @param keyword 搜索关键词（匹配岗位名称/部门/描述）
     * @return 分页岗位数据
     */
    public Page<Position> page(long current, long size, String keyword) {
        Page<Position> page = new Page<>(current, size);
        LambdaQueryWrapper<Position> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Position::getTitle, keyword)
                    .or().like(Position::getDepartment, keyword)
                    .or().like(Position::getDescription, keyword);
        }
        wrapper.orderByDesc(Position::getCreatedAt);
        return positionMapper.selectPage(page, wrapper);
    }

    /**
     * 分页查询岗位列表（HR视图，仅查看本企业岗位）
     * <p>
     * 根据当前登录HR的companyId过滤，确保数据隔离。
     */
    public Page<Position> pageForHr(long current, long size, String keyword) {
        UserContext.LoginUser user = UserContext.get();
        Page<Position> page = new Page<>(current, size);
        LambdaQueryWrapper<Position> wrapper = new LambdaQueryWrapper<>();
        if (user != null && user.isHr() && user.getCompanyId() != null) {
            wrapper.eq(Position::getCompanyId, user.getCompanyId());
        }
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Position::getTitle, keyword)
                    .or().like(Position::getDepartment, keyword)
                    .or().like(Position::getDescription, keyword);
        }
        wrapper.orderByDesc(Position::getCreatedAt);
        return positionMapper.selectPage(page, wrapper);
    }

    /**
     * 根据ID获取岗位详情（优先走缓存）
     */
    public Position get(Long id) {
        String key = CACHE_KEY_PREFIX + id;
        if (cacheService != null) {
            Object cached = cacheService.get(key);
            if (cached instanceof Position) {
                return (Position) cached;
            }
        }
        Position p = positionMapper.selectById(id);
        if (p != null && cacheService != null) {
            cacheService.set(key, p, CACHE_TTL_SEC);
        }
        return p;
    }

    /**
     * 创建岗位
     * <p>
     * HR创建时自动关联所属企业信息（companyId、companyName、companyLogo）。
     */
    public Long create(Position position) {
        if (position.getStatus() == null) {
            position.setStatus("OPEN");
        }
        UserContext.LoginUser ctx = UserContext.get();
        if (ctx != null && ctx.getCompanyId() != null && position.getCompanyId() == null) {
            position.setCompanyId(ctx.getCompanyId());
            User user = userMapper.selectById(ctx.getUserId());
            if (user != null && StringUtils.hasText(user.getCompany())) {
                position.setCompanyName(user.getCompany());
            }
        }
        if (position.getCompanyId() != null && !StringUtils.hasText(position.getCompanyName())) {
            Company company = companyMapper.selectById(position.getCompanyId());
            if (company != null) {
                position.setCompanyName(company.getName());
                if (!StringUtils.hasText(position.getCompanyLogo())) {
                    position.setCompanyLogo(company.getLogo());
                }
            }
        }
        positionMapper.insert(position);
        return position.getId();
    }

    /**
     * 更新岗位信息（同步清除缓存）
     */
    public void update(Position position) {
        positionMapper.updateById(position);
        if (cacheService != null && position.getId() != null) {
            cacheService.delete(CACHE_KEY_PREFIX + position.getId());
        }
    }

    /**
     * 删除岗位（同步清除缓存）
     */
    public void delete(Long id) {
        positionMapper.deleteById(id);
        if (cacheService != null) {
            cacheService.delete(CACHE_KEY_PREFIX + id);
        }
    }
}
