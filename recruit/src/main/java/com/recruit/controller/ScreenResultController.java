package com.recruit.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruit.common.R;
import com.recruit.entity.ScreenResult;
import com.recruit.mapper.ScreenResultMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/screen-results")
@RequiredArgsConstructor
@Tag(name = "筛选结果", description = "查询筛选历史结果、单条详情及某投递记录最近一次筛选结论")
public class ScreenResultController {

    private final ScreenResultMapper screenResultMapper;

    @GetMapping
    @Operation(summary = "分页查询筛选结果", description = "可按 applicationId 精准过滤，默认按创建时间倒序")
    public R<Page<ScreenResult>> page(
            @Parameter(description = "页码，从1开始") @RequestParam(defaultValue = "1") long current,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "20") long size,
            @Parameter(description = "投递记录ID（精准过滤）") @RequestParam(required = false) Long applicationId,
            @Parameter(description = "是否只看通过：1=通过 0=不通过") @RequestParam(required = false) Integer pass) {
        LambdaQueryWrapper<ScreenResult> wrapper = new LambdaQueryWrapper<>();
        if (applicationId != null) {
            wrapper.eq(ScreenResult::getApplicationId, applicationId);
        }
        if (pass != null) {
            wrapper.eq(ScreenResult::getPass, pass);
        }
        wrapper.orderByDesc(ScreenResult::getCreatedAt);
        return R.ok(screenResultMapper.selectPage(new Page<>(current, size), wrapper));
    }

    @GetMapping("/{id}")
    @Operation(summary = "筛选结果详情")
    public R<ScreenResult> detail(@Parameter(description = "筛选结果ID") @PathVariable Long id) {
        return R.ok(screenResultMapper.selectById(id));
    }

    @GetMapping("/application/{applicationId}/latest")
    @Operation(summary = "查询某投递记录最近一次筛选结果")
    public R<ScreenResult> latest(@Parameter(description = "投递记录ID") @PathVariable Long applicationId) {
        LambdaQueryWrapper<ScreenResult> wrapper = new LambdaQueryWrapper<ScreenResult>()
                .eq(ScreenResult::getApplicationId, applicationId)
                .orderByDesc(ScreenResult::getCreatedAt)
                .last("LIMIT 1");
        ScreenResult sr = screenResultMapper.selectOne(wrapper);
        return R.ok(sr);
    }
}
