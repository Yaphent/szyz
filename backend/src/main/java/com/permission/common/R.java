package com.permission.common;

import lombok.Data;

/**
 * 统一响应结果
 */
@Data
public class R<T> {
    
    private int code;
    private String msg;
    private T data;
    
    public static <T> R<T> success() {
        return success(null);
    }
    
    public static <T> R<T> success(T data) {
        return success(data, "success");
    }
    
    public static <T> R<T> success(T data, String msg) {
        R<T> r = new R<>();
        r.setCode(200);
        r.setMsg(msg);
        r.setData(data);
        return r;
    }
    
    public static <T> R<T> error(String msg) {
        return error(500, msg);
    }
    
    public static <T> R<T> error(int code, String msg) {
        R<T> r = new R<>();
        r.setCode(code);
        r.setMsg(msg);
        return r;
    }
    
    public static <T> R<T> error() {
        return error("操作失败");
    }
}
