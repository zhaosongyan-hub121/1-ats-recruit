package com.recruit.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruit.entity.Candidate;
import com.recruit.mapper.CandidateMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class CandidateService {

    private static final String CACHE_KEY_PREFIX = "ats:cand:";
    private static final long CACHE_TTL_SEC = 30 * 60L;

    private final CandidateMapper candidateMapper;

    @Autowired(required = false)
    private CacheService cacheService;

    /** 分页查询，支持按姓名、技能、简历关键词模糊搜索 */
    public Page<Candidate> page(long current, long size, String keyword) {
        Page<Candidate> page = new Page<>(current, size);
        LambdaQueryWrapper<Candidate> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Candidate::getName, keyword)
                    .or().like(Candidate::getSkills, keyword)
                    .or().like(Candidate::getResumeText, keyword);
        }
        wrapper.orderByDesc(Candidate::getCreatedAt);
        return candidateMapper.selectPage(page, wrapper);
    }

    public Candidate get(Long id) {
        String key = CACHE_KEY_PREFIX + id;
        if (cacheService != null) {
            Object cached = cacheService.get(key);
            if (cached instanceof Candidate) {
                return (Candidate) cached;
            }
        }
        Candidate c = candidateMapper.selectById(id);
        if (c != null && cacheService != null) {
            cacheService.set(key, c, CACHE_TTL_SEC);
        }
        return c;
    }

    public Long create(Candidate candidate) {
        candidateMapper.insert(candidate);
        return candidate.getId();
    }

    public void update(Candidate candidate) {
        candidateMapper.updateById(candidate);
        if (cacheService != null && candidate.getId() != null) {
            cacheService.delete(CACHE_KEY_PREFIX + candidate.getId());
        }
    }

    public void delete(Long id) {
        candidateMapper.deleteById(id);
        if (cacheService != null) {
            cacheService.delete(CACHE_KEY_PREFIX + id);
        }
    }
}
