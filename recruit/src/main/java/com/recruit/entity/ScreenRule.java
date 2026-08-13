package com.recruit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 简历筛选规则实体
 * <p>
 * 定义智能筛选引擎的匹配规则，支持三种规则类型：
 * <ul>
 *   <li>KEYWORD - 关键词匹配</li>
 *   <li>SKILL - 技能标签匹配</li>
 *   <li>EXPERIENCE - 经验年限匹配</li>
 * </ul>
 * 规则可绑定到特定岗位，也可作为通用规则。
 */
@Data
@TableName("screen_rule")
public class ScreenRule {

    /** 规则ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 规则名称 */
    private String name;

    /** 规则类型：KEYWORD / SKILL / EXPERIENCE */
    private String ruleType;

    /** 匹配目标字段：skills / resume_text / experience_years */
    private String targetField;

    /** 期望值，逗号分隔 */
    private String expectedValues;

    /** 匹配模式：ANY=任一匹配 / ALL=全匹配 / MIN=最小值 */
    private String matchMode;

    /** 权重分值 */
    private Integer weight;

    /** 是否启用：0-禁用，1-启用 */
    private Integer enabled;

    /** 绑定岗位ID（null表示通用规则） */
    private Long positionId;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 逻辑删除标记：0-正常，1-已删除 */
    @TableLogic
    private Integer deleted;
}
