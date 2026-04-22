package com.permission.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;

/**
 * 标签字典实体
 */
@Data
@TableName("t_dict_tag")
public class DictTag implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 主键ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 标签名 */
    private String tagName;

    /** 1系统预置 0自定义 */
    private Integer isSystem;

    /** 排序 */
    private Integer sortNo;
}
