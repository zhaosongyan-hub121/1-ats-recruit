package com.recruit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruit.common.R;
import com.recruit.entity.Position;
import com.recruit.service.PositionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/positions")
@RequiredArgsConstructor
@Tag(name = "职位管理", description = "职位的增删改查与模糊搜索")
public class PositionController {

    private final PositionService positionService;

    @GetMapping
    @Operation(summary = "分页查询职位", description = "支持关键词模糊搜索（标题/描述/要求），按创建时间倒序")
    public R<Page<Position>> page(
            @Parameter(description = "页码，从1开始") @RequestParam(defaultValue = "1") long current,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") long size,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {
        return R.ok(positionService.page(current, size, keyword));
    }

    @GetMapping("/{id}")
    @Operation(summary = "职位详情")
    public R<Position> get(@Parameter(description = "职位ID") @PathVariable Long id) {
        return R.ok(positionService.get(id));
    }

    @PostMapping
    @Operation(summary = "新建职位")
    public R<Long> create(@RequestBody Position position) {
        return R.ok(positionService.create(position));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新职位")
    public R<Void> update(
            @Parameter(description = "职位ID") @PathVariable Long id,
            @RequestBody Position position) {
        position.setId(id);
        positionService.update(position);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除职位（逻辑删除）")
    public R<Void> delete(@Parameter(description = "职位ID") @PathVariable Long id) {
        positionService.delete(id);
        return R.ok();
    }
}
