package com.permission.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.permission.common.PageResult;
import com.permission.dto.DocumentDTO;
import com.permission.dto.DocumentQueryDTO;
import com.permission.dto.DocumentVO;
import com.permission.dto.FileUploadVO;
import com.permission.entity.Document;
import com.permission.entity.DocumentFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

/**
 * 法规文档服务
 */
public interface DocumentService extends IService<Document> {

    /** 分页查询 */
    PageResult<DocumentVO> pageQuery(DocumentQueryDTO query);

    /** 根据ID查询详情(含文件、标签) */
    DocumentVO detail(Long id);

    /** 新增 */
    Long create(DocumentDTO dto);

    /** 更新 */
    void update(Long id, DocumentDTO dto);

    /** 逻辑删除(单条) */
    void remove(Long id);

    /** 批量删除 */
    void removeBatch(List<Long> ids);

    /** 批量修改状态 */
    void updateStatus(List<Long> ids, Integer status);

    /**
     * 上传文件(主文件/附件)
     *
     * @param file        文件
     * @param category    1主文件 2附件
     */
    FileUploadVO uploadFile(MultipartFile file, Integer category);

    /** 下载文件输入流 */
    InputStream downloadFile(Long fileId, DocumentFile[] holder);

    /** 删除文件(同时删除服务器文件) */
    void removeFile(Long fileId);

    /** 对文档进行向量化处理 */
    void vectorizeDocument(Long id);

    /** 删除文档的向量化数据 */
    void deleteVectorizedDocument(Long id);
}
