package com.recruit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 投递记录实体
 * <p>
 * 存储求职者投递岗位的完整生命周期记录，状态流转：
 * SUBMITTED → SCREENING_PASS → INTERVIEWING → OFFER → ACCEPTED
 * 任一阶段可转为 REJECTED
 */
@Data
@TableName("application")
public class Application {

    /** 投递记录ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 候选人档案ID */
    private Long candidateId;

    /** 投递用户ID */
    private Long userId;

    /** 目标岗位ID */
    private Long positionId;

    /** 投递状态：SUBMITTED/SCREENING_PASS/INTERVIEWING/INTERVIEWED/OFFER/ACCEPTED/REJECTED */
    private String status;

    /** 当前面试轮次 */
    private Integer currentRound;

    /** 求职信 */
    private String coverLetter;

    /** HR备注 */
    private String hrRemark;

    /** 状态变更日志，JSON数组格式 */
    private String statusLog;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 逻辑删除标记：0-正常，1-已删除 */
    @TableLogic
    private Integer deleted;
}
