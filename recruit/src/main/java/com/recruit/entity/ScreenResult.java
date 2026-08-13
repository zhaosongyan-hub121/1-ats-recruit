package com.recruit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 简历筛选结果实体
 * <p>
 * 存储智能筛选引擎对投递记录的评分结果，
 * 包含总分、满分、是否通过以及每条规则的匹配详情（JSON格式）。
 */
@Data
@TableName("screen_result")
public class ScreenResult {

    /** 筛选结果ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 关联投递记录ID */
    private Long applicationId;

    /** 实际得分 */
    private Integer totalScore;

    /** 满分 */
    private Integer maxScore;

    /** 是否通过：0-不通过，1-通过 */
    private Integer pass;

    /** 规则匹配详情，JSON数组格式 */
    private String ruleDetails;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 逻辑删除标记：0-正常，1-已删除 */
    @TableLogic
    private Integer deleted;
}
