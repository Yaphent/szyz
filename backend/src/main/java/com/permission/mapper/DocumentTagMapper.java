package com.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.permission.entity.DocumentTag;
import org.apache.ibatis.annotations.Mapper;

/**
 * 文档-标签关联 Mapper
 */
@Mapper
public interface DocumentTagMapper extends BaseMapper<DocumentTag> {
}
