package com.recruit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("job_position")
public class Position {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    private String companyName;

    private String companyLogo;

    private String department;

    private String description;

    private String requirements;

    /** OPEN / CLOSED */
    private String status;

    /** CAMPUS(校招) / SOCIAL(社招) / INTERN(实习) */
    private String category;

    private String location;

    private String salary;

    private String education;

    private String experience;

    private Long publishUserId;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
