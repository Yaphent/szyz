package com.permission.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 系统参数实体
 */
@Data
@TableName("sys_config")
public class SysConfig implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    @TableId(type = IdType.AUTO)
    private Long configId;
    
    /**
     * 参数名称
     */
    private String configName;
    
    /**
     * 参数键名
     */
    private String configKey;
    
    /**
     * 参数值
     */
    private String configValue;
    
    /**
     * 参数类型: S-字符串, N-数值, B-布尔
     */
    private String configType;
    
    /**
     * 备注
     */
    private String remark;
    
    /**
     * 状态: 0-禁用, 1-启用
     */
    private Integer status;
    
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;
    
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;
}
