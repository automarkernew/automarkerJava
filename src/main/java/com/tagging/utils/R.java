package com.tagging.utils;
import com.tagging.enums.ResultCodeEnum;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 默认：
 * 1、无数据成功
 * 2、无数据失败
 * 3、无数据其他
 *
 * 自定义：
 * 链式编程，可设置 是否成功？状态码？信息？数据？
 */
@Data
public class R
{
    private Boolean success;

    private Integer code;

    private String message;

    private Map<String, Object> data = new HashMap<>();

    /**
     * 构造器私有
     */
    private R() {
    }

    /**
     * 通用返回成功
     */
    public static R ok() {
        R r = new R();
        r.setSuccess(ResultCodeEnum.SUCCESS.getSuccess());
        r.setCode(ResultCodeEnum.SUCCESS.getCode());
        r.setMessage(ResultCodeEnum.SUCCESS.getMessage());
        return r;
    }

    /**
     * 通用返回失败，未知错误
     *
     * @return
     */
    public static R error() {
        R r = new R();
        r.setSuccess(ResultCodeEnum.UNKNOWN_ERROR.getSuccess());
        r.setCode(ResultCodeEnum.UNKNOWN_ERROR.getCode());
        r.setMessage(ResultCodeEnum.UNKNOWN_ERROR.getMessage());
        return r;
    }

    /**
     * 设置结果，形参为结果枚举
     *
     * @param result
     * @return
     */
    public static R setResult(ResultCodeEnum result) {
        R r = new R();
        r.setSuccess(result.getSuccess());
        r.setCode(result.getCode());
        r.setMessage(result.getMessage());
        return r;
    }

    /**
     * ------------使用链式编程，返回类本身----------
     * 自定义返回数据
     *
     * @param map
     * @return
     */
    public R data(Map<String, Object> map) {
        this.setData(map);
        return this;
    }

    /**
     * 通用设置data
     *
     * @param key
     * @param value
     * @return
     */
    public R data(String key, Object value) {
        this.data.put(key, value);
        return this;
    }

    /**
     * 自定义状态信息
     *
     * @param message
     * @return
     */
    public R message(String message) {
        this.setMessage(message);
        return this;
    }

    /**
     * 自定义状态码
     *
     * @param code
     * @return
     */
    public R code(Integer code) {
        this.setCode(code);
        return this;
    }

    /**
     * 自定义返回结果
     *
     * @param success
     * @return
     */
    public R success(Boolean success) {
        this.setSuccess(success);
        return this;
    }
}
