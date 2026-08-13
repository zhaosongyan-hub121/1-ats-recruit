package com.recruit.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.recruit.entity.Company;
import com.recruit.mapper.CompanyMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyMapper companyMapper;

    public Page<Company> page(long current, long size, String keyword) {
        Page<Company> page = new Page<>(current, size);
        LambdaQueryWrapper<Company> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(keyword)) {
            wrapper.like(Company::getName, keyword)
                    .or().like(Company::getIndustry, keyword);
        }
        wrapper.orderByDesc(Company::getCreatedAt);
        return companyMapper.selectPage(page, wrapper);
    }

    public Company get(Long id) {
        return companyMapper.selectById(id);
    }

    public Long create(Company company) {
        if (!StringUtils.hasText(company.getName())) {
            throw new com.recruit.common.BusinessException(400, "公司名称不能为空");
        }
        companyMapper.insert(company);
        return company.getId();
    }

    public void update(Company company) {
        Company existing = companyMapper.selectById(company.getId());
        if (existing == null) {
            throw new com.recruit.common.BusinessException(404, "公司不存在");
        }
        if (StringUtils.hasText(company.getName())) existing.setName(company.getName());
        if (StringUtils.hasText(company.getLogo())) existing.setLogo(company.getLogo());
        if (StringUtils.hasText(company.getLogoColor())) existing.setLogoColor(company.getLogoColor());
        if (StringUtils.hasText(company.getIndustry())) existing.setIndustry(company.getIndustry());
        if (StringUtils.hasText(company.getDescription())) existing.setDescription(company.getDescription());
        if (StringUtils.hasText(company.getLocation())) existing.setLocation(company.getLocation());
        if (StringUtils.hasText(company.getWebsite())) existing.setWebsite(company.getWebsite());
        if (company.getSize() != null) existing.setSize(company.getSize());
        companyMapper.updateById(existing);
    }

    public void delete(Long id) {
        companyMapper.deleteById(id);
    }
}