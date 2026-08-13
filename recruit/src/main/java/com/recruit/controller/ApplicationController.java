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

@RestController
@RequestMapping("/api/applications")
@RequiredArgsConstructor
@Tag(name = "投递记录", description = "候选人投递职位、状态流转")
public class ApplicationController {

    private final ApplicationService applicationService;

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

    @GetMapping("/{id}")
    @Operation(summary = "投递详情")
    public R<Application> get(@Parameter(description = "投递ID") @PathVariable Long id) {
        return R.ok(applicationService.get(id));
    }

    @PostMapping
    @Operation(summary = "候选人投递职位")
    public R<Long> apply(@RequestBody Application application) {
        return R.ok(applicationService.apply(application));
    }

    @PutMapping("/{id}/status")
    @Operation(summary = "更新投递状态")
    public R<Void> updateStatusByQuery(
            @Parameter(description = "投递ID") @PathVariable Long id,
            @Parameter(description = "新状态") @RequestParam String status) {
        applicationService.updateStatus(id, status);
        return R.ok();
    }

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

    @PutMapping("/{id}/remark")
    @Operation(summary = "HR添加备注")
    public R<Void> updateRemark(
            @Parameter(description = "投递ID") @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String remark = body.get("remark");
        applicationService.updateHrRemark(id, remark);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除投递记录")
    public R<Void> delete(@Parameter(description = "投递ID") @PathVariable Long id) {
        applicationService.delete(id);
        return R.ok();
    }
}
