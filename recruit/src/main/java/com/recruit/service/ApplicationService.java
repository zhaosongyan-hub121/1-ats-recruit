package com.recruit.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruit.common.BusinessException;
import com.recruit.entity.Application;
import com.recruit.mapper.ApplicationMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationMapper applicationMapper;

    /** 分页查询投递记录，可按候选人 ID、职位 ID、状态过滤 */
    public Page<Application> page(long current, long size, Long candidateId, Long positionId, String status) {
        Page<Application> page = new Page<>(current, size);
        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<>();
        if (candidateId != null) {
            wrapper.eq(Application::getCandidateId, candidateId);
        }
        if (positionId != null) {
            wrapper.eq(Application::getPositionId, positionId);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Application::getStatus, status);
        }
        wrapper.orderByDesc(Application::getCreatedAt);
        return applicationMapper.selectPage(page, wrapper);
    }

    public Application get(Long id) {
        return applicationMapper.selectById(id);
    }

    /** 候选人投递职位 */
    public Long apply(Application application) {
        if (application.getStatus() == null) {
            application.setStatus("PENDING");
        }
        applicationMapper.insert(application);
        return application.getId();
    }

    /** 更新投递状态：PENDING/REVIEWED/ACCEPTED/REJECTED */
    public void updateStatus(Long id, String status) {
        Application application = applicationMapper.selectById(id);
        if (application == null) {
            throw new BusinessException(404, "投递记录不存在");
        }
        application.setStatus(status);
        applicationMapper.updateById(application);
    }

    public void delete(Long id) {
        applicationMapper.deleteById(id);
    }
}
