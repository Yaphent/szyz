package com.permission.common;

import lombok.Data;
import java.util.List;

/**
 * 分页结果
 */
@Data
public class PageResult<T> {
    
    private List<T> list;
    private long total;
    private int page;
    private int pageSize;
    
    public PageResult() {}
    
    public PageResult(List<T> list, long total, int page, int pageSize) {
        this.list = list;
        this.total = total;
        this.page = page;
        this.pageSize = pageSize;
    }
}
