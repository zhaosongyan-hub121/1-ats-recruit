package com.recruit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 招聘岗位实体
 * <p>
 * 存储企业发布的招聘岗位信息，支持社招/校招/实习三种类型。
 * 岗位状态：OPEN(招聘中) / CLOSED(已关闭)
 * 岗位分类：SOCIAL(社招) / CAMPUS(校招) / INTERN(实习)
 */
@Data
@TableName("job_position")
public class Position {

    /** 岗位ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 岗位名称 */
    private String title;

    /** 发布企业名称 */
    private String companyName;

    /** 发布企业ID，关联company表 */
    private Long companyId;

    /** 企业Logo文字标识 */
    private String companyLogo;

    /** 所属部门 */
    private String department;

    /** 岗位描述 */
    private String description;

    /** 任职要求 */
    private String requirements;

    /** 岗位状态：OPEN(招聘中) / CLOSED(已关闭) */
    private String status;

    /** 岗位分类：SOCIAL(社招) / CAMPUS(校招) / INTERN(实习) */
    private String category;

    /** 工作地点 */
    private String location;

    /** 薪资范围，如"25-45K·16薪" */
    private String salary;

    /** 学历要求 */
    private String education;

    /** 经验要求，如"3-5年" */
    private String experience;

    /** 发布者用户ID */
    private Long publishUserId;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 逻辑删除标记：0-正常，1-已删除 */
    @TableLogic
    private Integer deleted;
}
