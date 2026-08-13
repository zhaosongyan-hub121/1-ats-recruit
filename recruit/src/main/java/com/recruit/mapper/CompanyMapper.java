package com.recruit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.recruit.entity.Company;
import org.apache.ibatis.annotations.Mapper;

/**
 * 企业数据访问层
 */
@Mapper
public interface CompanyMapper extends BaseMapper<Company> {
}