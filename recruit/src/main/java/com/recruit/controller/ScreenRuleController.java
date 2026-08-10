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
 * 筛选规则管理
 */
@RestController
@RequestMapping("/api/screen-rules")
@RequiredArgsConstructor
@Tag(name = "筛选规则管理")
public class ScreenRuleController {

    private final ScreenRuleService screenRuleService;

    @GetMapping
    @Operation(summary = "分页查询筛选规则")
    public R<Page<ScreenRule>> page(@RequestParam(defaultValue = "1") long current,
                                    @RequestParam(defaultValue = "10") long size,
                                    @RequestParam(required = false) Long positionId,
                                    @RequestParam(required = false) Integer enabled) {
        return R.ok(screenRuleService.page(current, size, positionId, enabled));
    }

    @GetMapping("/{id}")
    @Operation(summary = "规则详情")
    public R<ScreenRule> get(@PathVariable Long id) {
        return R.ok(screenRuleService.get(id));
    }

    @PostMapping
    @Operation(summary = "新建筛选规则")
    public R<Long> create(@RequestBody ScreenRule rule) {
        return R.ok(screenRuleService.create(rule));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新筛选规则")
    public R<Void> update(@PathVariable Long id, @RequestBody ScreenRule rule) {
        rule.setId(id);
        screenRuleService.update(rule);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除筛选规则")
    public R<Void> delete(@PathVariable Long id) {
        screenRuleService.delete(id);
        return R.ok();
    }
}
