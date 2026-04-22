package com.permission.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.permission.entity.DictTag;
import com.permission.exception.BusinessException;
import com.permission.mapper.DictTagMapper;
import com.permission.service.DictTagService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 标签字典服务实现
 */
@Service
public class DictTagServiceImpl extends ServiceImpl<DictTagMapper, DictTag> implements DictTagService {

    @Override
    public List<DictTag> listAll() {
        return this.list(new LambdaQueryWrapper<DictTag>()
                .orderByAsc(DictTag::getSortNo)
                .orderByAsc(DictTag::getId));
    }

    @Override
    public DictTag addCustom(String tagName) {
        if (!StringUtils.hasText(tagName)) {
            throw new BusinessException("标签名不能为空");
        }
        // 检查重复
        Long count = this.count(new LambdaQueryWrapper<DictTag>()
                .eq(DictTag::getTagName, tagName.trim()));
        if (count != null && count > 0) {
            throw new BusinessException("标签已存在");
        }
        DictTag tag = new DictTag();
        tag.setTagName(tagName.trim());
        tag.setIsSystem(0);
        tag.setSortNo(99);
        this.save(tag);
        return tag;
    }
}
