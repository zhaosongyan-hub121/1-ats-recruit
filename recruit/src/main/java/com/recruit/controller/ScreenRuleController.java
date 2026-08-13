package com.recruit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruit.common.R;
import com.recruit.entity.ScreenRule;
import com.recruit.service.ScreenRuleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 智能筛选规则管理控制器
 * <p>
 * 维护智能筛选引擎所使用的匹配规则，支持按岗位绑定规则的增删改查与分页查询，
 * 可按 positionId 与启用状态 enabled 过滤。规则用于在筛选时计算候选人匹配得分。
 * </p>
 */
@RestController
@RequestMapping("/api/screen-rules")
@RequiredArgsConstructor
@Tag(name = "筛选规则管理")
public class ScreenRuleController {

    private final ScreenRuleService screenRuleService;

    /**
     * 分页查询筛选规则
     *
     * @param current    页码，从 1 开始
     * @param size       每页条数
     * @param positionId 岗位 ID，可为空
     * @param enabled    启用状态，1=启用 0=禁用，可为空
     * @return 筛选规则分页数据
     */
    @GetMapping
    @Operation(summary = "分页查询筛选规则")
    public R<Page<ScreenRule>> page(@RequestParam(defaultValue = "1") long current,
                                    @RequestParam(defaultValue = "10") long size,
                                    @RequestParam(required = false) Long positionId,
                                    @RequestParam(required = false) Integer enabled) {
        return R.ok(screenRuleService.page(current, size, positionId, enabled));
    }

    /**
     * 根据 ID 获取筛选规则详情
     *
     * @param id 规则 ID
     * @return 筛选规则详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "规则详情")
    public R<ScreenRule> get(@PathVariable Long id) {
        return R.ok(screenRuleService.get(id));
    }

    /**
     * 新建筛选规则
     *
     * @param rule 规则内容
     * @return 新建规则的 ID
     */
    @PostMapping
    @Operation(summary = "新建筛选规则")
    public R<Long> create(@RequestBody ScreenRule rule) {
        return R.ok(screenRuleService.create(rule));
    }

    /**
     * 更新筛选规则
     *
     * @param id   规则 ID（以路径参数为准）
     * @param rule 规则更新内容
     * @return 空结果
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新筛选规则")
    public R<Void> update(@PathVariable Long id, @RequestBody ScreenRule rule) {
        rule.setId(id);
        screenRuleService.update(rule);
        return R.ok();
    }

    /**
     * 删除筛选规则
     *
     * @param id 规则 ID
     * @return 空结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除筛选规则")
    public R<Void> delete(@PathVariable Long id) {
        screenRuleService.delete(id);
        return R.ok();
    }
}
