package com.recruit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruit.common.BusinessException;
import com.recruit.common.R;
import com.recruit.entity.Company;
import com.recruit.security.UserContext;
import com.recruit.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 企业管理控制器
 * <p>
 * 提供企业信息的CRUD接口。
 * 安全策略：HR仅能查看企业信息，只有管理员(ADMIN)可创建/修改/删除企业。
 */
@RestController
@RequestMapping("/api/companies")
@RequiredArgsConstructor
@Tag(name = "公司管理", description = "公司信息的增删改查与列表查询")
public class CompanyController {

    private final CompanyService companyService;

    @GetMapping
    @Operation(summary = "分页查询公司列表")
    public R<Page<Company>> page(
            @Parameter(description = "页码") @RequestParam(defaultValue = "1") long current,
            @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") long size,
            @Parameter(description = "搜索关键词") @RequestParam(required = false) String keyword) {
        return R.ok(companyService.page(current, size, keyword));
    }

    @GetMapping("/all")
    @Operation(summary = "获取所有公司列表（不分页，用于下拉选择）")
    public R<List<Company>> all() {
        return R.ok(companyService.page(1, 1000, null).getRecords());
    }

    @GetMapping("/{id}")
    @Operation(summary = "公司详情")
    public R<Company> get(@PathVariable Long id) {
        return R.ok(companyService.get(id));
    }

    @PostMapping
    @Operation(summary = "新建公司（仅管理员）")
    public R<Long> create(@RequestBody Company company) {
        assertAdmin();
        return R.ok(companyService.create(company));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新公司信息（仅管理员）")
    public R<Void> update(@PathVariable Long id, @RequestBody Company company) {
        assertAdmin();
        company.setId(id);
        companyService.update(company);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除公司（仅管理员）")
    public R<Void> delete(@PathVariable Long id) {
        assertAdmin();
        companyService.delete(id);
        return R.ok();
    }

    /**
     * 校验当前登录用户是否为管理员，非管理员抛出403异常
     */
    private void assertAdmin() {
        UserContext.LoginUser user = UserContext.get();
        if (user == null || !"ADMIN".equals(user.getRole())) {
            throw new BusinessException(403, "无权限操作，仅管理员可管理企业信息");
        }
    }
}