package com.recruit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LoginResponse {

    private String token;
    private Long userId;
    private String username;
    private String realName;
    private String role;
    private String email;
    private String phone;
    private String company;
    private String avatar;
}
