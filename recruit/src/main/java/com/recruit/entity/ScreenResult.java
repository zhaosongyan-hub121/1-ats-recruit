package com.recruit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("screen_result")
public class ScreenResult {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long applicationId;

    private Integer totalScore;

    private Integer maxScore;

    /** 0=不通过 1=通过 */
    private Integer pass;

    /** JSON 格式的规则匹配详情 */
    private String ruleDetails;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
