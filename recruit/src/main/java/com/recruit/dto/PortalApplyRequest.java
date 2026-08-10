package com.recruit.dto;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
public class PortalApplyRequest {

    @NotNull(message = "职位ID不能为空")
    private Long positionId;

    @NotBlank(message = "姓名不能为空")
    private String name;

    private String email;

    private String phone;

    private String skills;

    private Integer experienceYears;

    private String resumeText;

    private String coverLetter;
}
