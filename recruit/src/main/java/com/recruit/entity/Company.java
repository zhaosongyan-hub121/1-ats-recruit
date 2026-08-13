package com.recruit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 企业实体
 * <p>
 * 存储入驻企业信息，一个企业可绑定多个HR账号，
 * 企业与岗位通过companyId关联，实现企业-岗位-HR的层级管理。
 */
@Data
@TableName("company")
public class Company {

    /** 企业ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 企业名称，唯一 */
    private String name;

    /** Logo文字标识（2字简称） */
    private String logo;

    /** Logo背景色（十六进制） */
    private String logoColor;

    /** 所属行业 */
    private String industry;

    /** 企业简介 */
    private String description;

    /** 企业所在地 */
    private String location;

    /** 企业官网 */
    private String website;

    /** 企业规模，如"100-499人" */
    private String size;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 逻辑删除标记：0-正常，1-已删除 */
    @TableLogic
    private Integer deleted;
}