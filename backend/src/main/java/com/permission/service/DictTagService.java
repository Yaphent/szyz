package com.permission.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.permission.entity.DictTag;

import java.util.List;

/**
 * 标签字典服务
 */
public interface DictTagService extends IService<DictTag> {

    /** 查询全部标签(系统+自定义) */
    List<DictTag> listAll();

    /** 新增自定义标签 */
    DictTag addCustom(String tagName);
}
