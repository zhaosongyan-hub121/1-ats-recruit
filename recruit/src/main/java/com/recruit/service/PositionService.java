package com.recruit.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruit.entity.Position;
import com.recruit.mapper.PositionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class PositionService {

    private static final String CACHE_KEY_PREFIX = "ats:pos:";
    private static final long CACHE_TTL_SEC = 30 * 60L;

    private final PositionMapper positionMapper;

    @Autowired(required = false)
    private CacheService cacheService;

    /** 分页查询，支持按标题或部门模糊搜索 */
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

    public Long create(Position position) {
        if (position.getStatus() == null) {
            position.setStatus("OPEN");
        }
        positionMapper.insert(position);
        return position.getId();
    }

    public void update(Position position) {
        positionMapper.updateById(position);
        if (cacheService != null && position.getId() != null) {
            cacheService.delete(CACHE_KEY_PREFIX + position.getId());
        }
    }

    public void delete(Long id) {
        positionMapper.deleteById(id);
        if (cacheService != null) {
            cacheService.delete(CACHE_KEY_PREFIX + id);
        }
    }
}
