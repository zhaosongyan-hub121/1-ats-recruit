package com.recruit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 系统用户实体
 * <p>
 * 存储平台所有用户账号信息，支持四种角色：
 * <ul>
 *   <li>ADMIN - 超级管理员，拥有全局管理权限</li>
 *   <li>HR - 企业HR，绑定所属企业，管理本企业招聘业务</li>
 *   <li>CANDIDATE - 求职者，浏览岗位、投递简历</li>
 * </ul>
 */
@Data
@TableName("sys_user")
public class User {

    /** 用户ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 登录账号，唯一 */
    private String username;

    /** BCrypt加密后的密码 */
    private String password;

    /** 真实姓名 */
    private String realName;

    /** 邮箱 */
    private String email;

    /** 手机号 */
    private String phone;

    /** 角色：ADMIN / HR / CANDIDATE */
    private String role;

    /** 所属企业名称（HR角色使用） */
    private String company;

    /** 所属企业ID（HR角色使用，关联company表） */
    private Long companyId;

    /** 头像URL */
    private String avatar;

    /** 创建时间 */
    private LocalDateTime createdAt;

    /** 更新时间 */
    private LocalDateTime updatedAt;

    /** 逻辑删除标记：0-正常，1-已删除 */
    @TableLogic
    private Integer deleted;
}
