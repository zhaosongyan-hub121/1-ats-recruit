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

/**
 * 候选人档案控制器
 * <p>
 * 管理求职者简历信息（姓名、技能、经验年限、简历文本等），
 * 提供增删改查与分页搜索能力，支持按姓名/技能/简历文本关键词模糊匹配。
 * </p>
 */
@RestController
@RequestMapping("/api/candidates")
@RequiredArgsConstructor
@Tag(name = "候选人管理", description = "候选人信息增删改查与关键词搜索")
public class CandidateController {

    private final CandidateService candidateService;

    /**
     * 分页查询候选人列表
     * <p>支持按姓名、技能、简历文本进行关键词模糊搜索。</p>
     *
     * @param current 页码，从 1 开始
     * @param size    每页条数
     * @param keyword 搜索关键词，可为空
     * @return 候选人分页数据
     */
    @GetMapping
    @Operation(summary = "分页查询候选人", description = "支持关键词搜索（姓名/技能/简历文本）")
    public R<Page<Candidate>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") long current,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") long size,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {
        return R.ok(candidateService.page(current, size, keyword));
    }

    /**
     * 根据 ID 获取候选人详情
     *
     * @param id 候选人 ID
     * @return 候选人详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "候选人详情")
    public R<Candidate> get(@Parameter(description = "候选人ID") @PathVariable Long id) {
        return R.ok(candidateService.get(id));
    }

    /**
     * 新建候选人档案
     *
     * @param candidate 候选人信息
     * @return 新建候选人的 ID
     */
    @PostMapping
    @Operation(summary = "新建候选人")
    public R<Long> create(@RequestBody Candidate candidate) {
        return R.ok(candidateService.create(candidate));
    }

    /**
     * 更新候选人档案
     *
     * @param id         候选人 ID（以路径参数为准）
     * @param candidate 候选人更新内容
     * @return 空结果
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新候选人")
    public R<Void> update(
            @Parameter(description = "候选人ID") @PathVariable Long id,
            @RequestBody Candidate candidate) {
        candidate.setId(id);
        candidateService.update(candidate);
        return R.ok();
    }

    /**
     * 删除候选人（逻辑删除）
     *
     * @param id 候选人 ID
     * @return 空结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除候选人（逻辑删除）")
    public R<Void> delete(@Parameter(description = "候选人ID") @PathVariable Long id) {
        candidateService.delete(id);
        return R.ok();
    }
}
