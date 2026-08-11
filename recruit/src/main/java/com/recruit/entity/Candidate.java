package com.recruit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("candidate")
public class Candidate {

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private String name;

    private String email;

    private String phone;

    private String skills;

    private Integer experienceYears;

    private String resumeText;

    private String educationLevel;

    private String school;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
