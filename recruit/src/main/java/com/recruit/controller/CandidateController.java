package com.recruit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruit.common.R;
import com.recruit.entity.Candidate;
import com.recruit.service.CandidateService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/candidates")
@RequiredArgsConstructor
@Tag(name = "候选人管理", description = "候选人信息增删改查与关键词搜索")
public class CandidateController {

    private final CandidateService candidateService;

    @GetMapping
    @Operation(summary = "分页查询候选人", description = "支持关键词搜索（姓名/技能/简历文本）")
    public R<Page<Candidate>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") long current,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") long size,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {
        return R.ok(candidateService.page(current, size, keyword));
    }

    @GetMapping("/{id}")
    @Operation(summary = "候选人详情")
    public R<Candidate> get(@Parameter(description = "候选人ID") @PathVariable Long id) {
        return R.ok(candidateService.get(id));
    }

    @PostMapping
    @Operation(summary = "新建候选人")
    public R<Long> create(@RequestBody Candidate candidate) {
        return R.ok(candidateService.create(candidate));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新候选人")
    public R<Void> update(
            @Parameter(description = "候选人ID") @PathVariable Long id,
            @RequestBody Candidate candidate) {
        candidate.setId(id);
        candidateService.update(candidate);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除候选人（逻辑删除）")
    public R<Void> delete(@Parameter(description = "候选人ID") @PathVariable Long id) {
        candidateService.delete(id);
        return R.ok();
    }
}
