package com.recruit.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 岗位收藏实体
 * <p>
 * 记录求职者收藏的岗位，用于个人中心的收藏列表展示。
 */
@Data
@TableName("favorite")
public class Favorite {

    /** 收藏ID，自增主键 */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户ID */
    private Long userId;

    /** 岗位ID */
    private Long positionId;

    /** 收藏时间 */
    private LocalDateTime createdAt;
}
