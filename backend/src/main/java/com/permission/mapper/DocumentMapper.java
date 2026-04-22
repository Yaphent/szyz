package com.permission.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.permission.dto.DocumentQueryDTO;
import com.permission.dto.DocumentVO;
import com.permission.entity.Document;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 法规文档 Mapper
 */
@Mapper
public interface DocumentMapper extends BaseMapper<Document> {

    /**
     * 分页查询（带筛选条件）
     */
    IPage<DocumentVO> selectDocumentPage(IPage<DocumentVO> page,
                                         @Param("q") DocumentQueryDTO query);
}
