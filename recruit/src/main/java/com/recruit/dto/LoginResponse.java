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
    private Long companyId;

    public LoginResponse(String token, Long userId, String username, String realName,
                        String role, String email, String phone, String company, String avatar) {
        this.token = token;
        this.userId = userId;
        this.username = username;
        this.realName = realName;
        this.role = role;
        this.email = email;
        this.phone = phone;
        this.company = company;
        this.avatar = avatar;
        this.companyId = null;
    }
}