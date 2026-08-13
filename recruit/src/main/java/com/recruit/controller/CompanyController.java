package com.recruit.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruit.common.R;
import com.recruit.entity.Company;
import com.recruit.service.CompanyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
    @Operation(summary = "新建公司")
    public R<Long> create(@RequestBody Company company) {
        return R.ok(companyService.create(company));
    }

    @PutMapping("/{id}")
    @Operation(summary = "更新公司信息")
    public R<Void> update(@PathVariable Long id, @RequestBody Company company) {
        company.setId(id);
        companyService.update(company);
        return R.ok();
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "删除公司")
    public R<Void> delete(@PathVariable Long id) {
        companyService.delete(id);
        return R.ok();
    }
}