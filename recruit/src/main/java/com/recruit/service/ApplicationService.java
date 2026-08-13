package com.recruit.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruit.common.BusinessException;
import com.recruit.entity.Application;
import com.recruit.entity.Position;
import com.recruit.mapper.ApplicationMapper;
import com.recruit.mapper.PositionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 投递记录服务
 * <p>
 * 负责简历投递、状态流转、HR备注等业务逻辑。
 * 支持管理员全量查询和HR按公司隔离查询（通过岗位ID关联）。
 */
@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final ApplicationMapper applicationMapper;
    private final PositionMapper positionMapper;

    /**
     * 分页查询投递记录（管理员视图）
     *
     * @param current    页码
     * @param size       每页条数
     * @param candidateId 候选人ID（可选）
     * @param positionId  岗位ID（可选）
     * @param status     投递状态（可选）
     * @return 分页投递数据
     */
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

    /**
     * 分页查询投递记录（HR视图，仅查看本企业岗位的投递）
     * <p>
     * 通过companyId查询该企业所有岗位ID，再按岗位ID过滤投递记录。
     */
    public Page<Application> pageForHr(long current, long size, Long companyId, String status) {
        Page<Application> page = new Page<>(current, size);
        LambdaQueryWrapper<Application> wrapper = new LambdaQueryWrapper<>();

        if (companyId != null) {
            List<Position> positions = positionMapper.selectList(
                    new LambdaQueryWrapper<Position>().eq(Position::getCompanyId, companyId));
            List<Long> positionIds = positions.stream().map(Position::getId).collect(Collectors.toList());
            if (positionIds.isEmpty()) {
                return new Page<>(current, size);
            }
            wrapper.in(Application::getPositionId, positionIds);
        }
        if (StringUtils.hasText(status)) {
            wrapper.eq(Application::getStatus, status);
        }
        wrapper.orderByDesc(Application::getCreatedAt);
        return applicationMapper.selectPage(page, wrapper);
    }

    /**
     * 根据ID查询投递记录
     */
    public Application get(Long id) {
        return applicationMapper.selectById(id);
    }

    /**
     * 创建投递记录
     * <p>
     * 默认状态为SUBMITTED（已投递），初始化第一轮次，
     * 并写入状态日志。
     */
    public Long apply(Application application) {
        if (application.getStatus() == null) {
            application.setStatus("SUBMITTED");
        }
        if (application.getCurrentRound() == null) {
            application.setCurrentRound(1);
        }
        if (!StringUtils.hasText(application.getStatusLog())) {
            application.setStatusLog("[{\"time\":\"" + LocalDateTime.now() + "\",\"status\":\"SUBMITTED\",\"note\":\"简历投递成功\"}]");
        }
        applicationMapper.insert(application);
        return application.getId();
    }

    /**
     * 更新投递状态
     * <p>
     * 变更状态时自动追加状态变更日志，保留完整流转历史。
     */
    public void updateStatus(Long id, String status) {
        Application application = applicationMapper.selectById(id);
        if (application == null) {
            throw new BusinessException(404, "投递记录不存在");
        }
        application.setStatus(status);
        String logEntry = "[{\"time\":\"" + LocalDateTime.now() + "\",\"status\":\"" + status + "\"}]";
        if (StringUtils.hasText(application.getStatusLog())) {
            application.setStatusLog(application.getStatusLog() + "," + logEntry.substring(1));
        } else {
            application.setStatusLog("[" + logEntry.substring(1));
        }
        applicationMapper.updateById(application);
    }

    /**
     * 更新HR备注
     */
    public void updateHrRemark(Long id, String remark) {
        Application application = applicationMapper.selectById(id);
        if (application == null) {
            throw new BusinessException(404, "投递记录不存在");
        }
        application.setHrRemark(remark);
        applicationMapper.updateById(application);
    }

    /**
     * 删除投递记录（逻辑删除）
     */
    public void delete(Long id) {
        applicationMapper.deleteById(id);
    }
}
