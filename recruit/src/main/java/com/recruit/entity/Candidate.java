package com.recruit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 候选人档案实体
 * <p>
 * 存储求职者的简历信息，与用户账号一对一关联。
 * 简历文本支持纯文本格式，用于智能筛选引擎匹配。
 */
@Data
@TableName("candidate")
public class Candidate {

    /** 候选人ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联用户ID */
    private Long userId;

    /** 姓名 */
    private String name;

    /** 邮箱 */
    private String email;

    /** 电话 */
    private String phone;

    /** 技能标签，逗号分隔 */
    private String skills;

    /** 工作年限 */
    private Integer experienceYears;

    /** 简历全文 */
    private String resumeText;

    /** 学历层次 */
    private String educationLevel;

    /** 毕业院校 */
    private String school;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 逻辑删除标记：0-正常，1-已删除 */
    @TableLogic
    private Integer deleted;
}
