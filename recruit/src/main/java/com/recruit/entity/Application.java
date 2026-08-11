package com.recruit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("application")
public class Application {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long candidateId;

    private Long userId;

    private Long positionId;

    /** SUBMITTED/SCREENING_PASS/INTERVIEWING/INTERVIEWED/OFFER/ACCEPTED/REJECTED */
    private String status;

    private Integer currentRound;

    private String coverLetter;

    private String hrRemark;

    private String statusLog;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
