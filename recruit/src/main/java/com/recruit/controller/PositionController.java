package com.recruit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruit.common.R;
import com.recruit.entity.Position;
import com.recruit.security.UserContext;
import com.recruit.service.PositionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 岗位管理控制器
 * <p>
 * 提供岗位的增删改查与分页查询能力，支持关键词模糊搜索。
 * 通过 role 参数实现数据隔离：管理员可查询全部岗位，HR 仅可见本公司岗位。
 * </p>
 */
@RestController
@RequestMapping("/api/positions")
@RequiredArgsConstructor
@Tag(name = "职位管理", description = "职位的增删改查与模糊搜索")
public class PositionController {

    private final PositionService positionService;

    /**
     * 分页查询岗位列表
     * <p>当 role 为 hr 时按当前 HR 所属公司隔离查询，否则查询全部岗位。</p>
     *
     * @param current 页码，从 1 开始
     * @param size    每页条数
     * @param keyword 关键词，用于职位名称等模糊匹配，可为空
     * @param role    角色过滤，传 hr 时启用公司隔离
     * @return 岗位分页数据
     */
    @GetMapping
    @Operation(summary = "分页查询职位", description = "支持关键词模糊搜索，管理员查看全部，HR仅查看本公司")
    public R<Page<Position>> page(
            @Parameter(description = "页码，从1开始") @RequestParam(defaultValue = "1") long current,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") long size,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword,
            @Parameter(description = "角色过滤") @RequestParam(required = false) String role) {
        if ("hr".equalsIgnoreCase(role)) {
            return R.ok(positionService.pageForHr(current, size, keyword));
        }
        return R.ok(positionService.page(current, size, keyword));
    }

    /**
     * 根据 ID 获取岗位详情
     *
     * @param id 岗位 ID
     * @return 岗位详情
     */
    @GetMapping("/{id}")
    @Operation(summary = "职位详情")
    public R<Position> get(@Parameter(description = "职位ID") @PathVariable Long id) {
        return R.ok(positionService.get(id));
    }

    /**
     * 新建岗位
     *
     * @param position 岗位信息
     * @return 新建岗位的 ID
     */
    @PostMapping
    @Operation(summary = "新建职位")
    public R<Long> create(@RequestBody Position position) {
        return R.ok(positionService.create(position));
    }

    /**
     * 更新岗位信息
     *
     * @param id       岗位 ID（以路径参数为准，覆盖 body 中的 id）
     * @param position 岗位更新内容
     * @return 空结果
     */
    @PutMapping("/{id}")
    @Operation(summary = "更新职位")
    public R<Void> update(
            @Parameter(description = "职位ID") @PathVariable Long id,
            @RequestBody Position position) {
        position.setId(id);
        positionService.update(position);
        return R.ok();
    }

    /**
     * 删除岗位（逻辑删除）
     *
     * @param id 岗位 ID
     * @return 空结果
     */
    @DeleteMapping("/{id}")
    @Operation(summary = "删除职位（逻辑删除）")
    public R<Void> delete(@Parameter(description = "职位ID") @PathVariable Long id) {
        positionService.delete(id);
        return R.ok();
    }
}
