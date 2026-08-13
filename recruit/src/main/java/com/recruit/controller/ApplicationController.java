package com.recruit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruit.common.R;
import com.recruit.entity.Application;
import com.recruit.security.UserContext;
import com.recruit.service.ApplicationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 投递记录控制器
 * <p>
 * 管理简历投递全流程：投递创建、状态流转（初筛/面试/Offer 等）以及 HR 备注维护。
 * 查询支持按候选人、职位、状态过滤；当传入 companyId 时启用 HR 视角的按公司隔离查询。
 * </p>
 */
@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Tag(name = "投递记录", description = "候选人投递职位、状态流转")
public class ApplicationController {

    private final ApplicationService applicationService;

    /**
     * 分页查询投递记录
     * <p>当 companyId 不为空时按公司隔离查询（HR 视角），否则按候选人/职位/状态条件查询。</p>
     *
     * @param current     页码，从 1 开始
     * @param size        每页条数
     * @param candidateId 候选人 ID，可为空
     * @param positionId  职位 ID，可为空
     * @param status      投递状态，可为空
     * @param companyId   公司 ID，非空时启用 HR 公司过滤
     * @return 投递记录分页数据
     */
    @GetMapping
    @Operation(summary = "分页查询投递记录", description = "管理员查看全部，HR按公司过滤")
    public R<Page<Application>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") long current,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") long size,
            @Parameter(description = "候选人ID") @RequestParam(required = false) Long candidateId,
            @Parameter(description = "职位ID") @RequestParam(required = false) Long positionId,
            @Parameter(description = "状态") @RequestParam(required = false) String status,
            @Parameter(description = "公司ID(HR过滤)") @RequestParam(required = false) Long companyId) {
        if (companyId != null) {
            return R.ok(applicationService.pageForHr(current, size, companyId, status));
        }
        return R.ok(applicationService.page(current, size, candidateId, positionId, status));
    }

    /**
     * 根据 ID 获取投递详情
     *
     * @param id 投递记录 ID
     * @return 投递详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "投递详情")
    public R<Application> get(@Parameter(description = "投递ID") @PathVariable Long id) {
        return R.ok(applicationService.get(id));
    }

    /**
     * 候选人投递职位
     *
     * @param application 投递信息（含候选人 ID、职位 ID 等）
     * @return 新建的投递记录 ID
     */
    @PostMapping
    @Operation(summary = "候选人投递职位")
    public R<Long> apply(@RequestBody Application application) {
        return R.ok(applicationService.apply(application));
    }

    /**
     * 通过 Query 参数更新投递状态
     *
     * @param id     投递记录 ID
     * @param status 新状态值
     * @return 空结果
     */
    @PutMapping("/{id}/status")
    @Operation(summary = "更新投递状态")
    public R<Void> updateStatusByQuery(
            @Parameter(description = "投递ID") @PathVariable Long id,
            @Parameter(description = "新状态") @RequestParam String status) {
        applicationService.updateStatus(id, status);
        return R.ok();
    }

    /**
     * 通过 Body 更新投递状态
     *
     * @param id   投递记录 ID
     * @param body 请求体，需包含非空的 status 字段
     * @return 空结果
     */
    @PutMapping("/{id}/status/body")
    @Operation(summary = "更新投递状态（Body）")
    public R<Void> updateStatusByBody(
            @Parameter(description = "投递ID") @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String status = body.get("status");
        if (!StringUtils.hasText(status)) {
            throw new IllegalArgumentException("status 必填");
        }
        applicationService.updateStatus(id, status);
        return R.ok();
    }

    /**
     * 更新 HR 备注
     *
     * @param id   投递记录 ID
     * @param body 请求体，需包含 remark 字段
     * @return 空结果
     */
    @PutMapping("/{id}/remark")
    @Operation(summary = "HR添加备注")
    public R<Void> updateRemark(
            @Parameter(description = "投递ID") @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String remark = body.get("remark");
        applicationService.updateHrRemark(id, remark);
        return R.ok();
    }

    /**
     * 删除投递记录
     *
     * @param id 投递记录 ID
     * @return 空结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除投递记录")
    public R<Void> delete(@Parameter(description = "投递ID") @PathVariable Long id) {
        applicationService.delete(id);
        return R.ok();
    }
}
