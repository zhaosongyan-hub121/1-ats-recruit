package com.recruit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("screen_rule")
public class ScreenRule {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    /** KEYWORD / SKILL / EXPERIENCE */
    private String ruleType;

    /** skills / resume_text / experience_years */
    private String targetField;

    /** 逗号分隔的期望值 */
    private String expectedValues;

    /** ANY=匹配任一即得分 / ALL=必须全匹配 / MIN=最小值 */
    private String matchMode;

    private Integer weight;

    private Integer enabled;

    /** 绑定职位（null=通用规则） */
    private Long positionId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
