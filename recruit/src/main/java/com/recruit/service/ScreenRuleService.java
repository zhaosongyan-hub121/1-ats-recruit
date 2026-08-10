package com.recruit.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruit.entity.ScreenRule;
import com.recruit.mapper.ScreenRuleMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ScreenRuleService {

    private final ScreenRuleMapper screenRuleMapper;

    public Page<ScreenRule> page(long current, long size, Long positionId, Integer enabled) {
        Page<ScreenRule> page = new Page<>(current, size);
        LambdaQueryWrapper<ScreenRule> wrapper = new LambdaQueryWrapper<>();
        if (positionId != null) {
            wrapper.eq(ScreenRule::getPositionId, positionId);
        }
        if (enabled != null) {
            wrapper.eq(ScreenRule::getEnabled, enabled);
        }
        wrapper.orderByDesc(ScreenRule::getCreatedAt);
        return screenRuleMapper.selectPage(page, wrapper);
    }

    public List<ScreenRule> listByPosition(Long positionId) {
        LambdaQueryWrapper<ScreenRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScreenRule::getEnabled, 1)
                .and(w -> w.eq(ScreenRule::getPositionId, positionId).or().isNull(ScreenRule::getPositionId))
                .orderByDesc(ScreenRule::getWeight);
        return screenRuleMapper.selectList(wrapper);
    }

    public ScreenRule get(Long id) {
        return screenRuleMapper.selectById(id);
    }

    public Long create(ScreenRule rule) {
        if (rule.getEnabled() == null) {
            rule.setEnabled(1);
        }
        if (rule.getWeight() == null) {
            rule.setWeight(10);
        }
        screenRuleMapper.insert(rule);
        return rule.getId();
    }

    public void update(ScreenRule rule) {
        screenRuleMapper.updateById(rule);
    }

    public void delete(Long id) {
        screenRuleMapper.deleteById(id);
    }
}
