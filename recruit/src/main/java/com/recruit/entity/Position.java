package com.recruit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 职位（表名 job_position 规避 MySQL 关键字 position）
 */
@Data
@TableName("job_position")
public class Position {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String department;

    private String description;

    private String requirements;

    /** OPEN / CLOSED */
    private String status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
