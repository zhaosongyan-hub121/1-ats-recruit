package com.recruit.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.recruit.entity.Favorite;
import org.apache.ibatis.annotations.Mapper;

/**
 * 岗位收藏数据访问层
 */
@Mapper
public interface FavoriteMapper extends BaseMapper<Favorite> {
}
