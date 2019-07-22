package com.walker.redis.common;

import lombok.Data;

import java.util.Map;

/**
 * @author walker
 * @date 2019/7/22
 */
@Data
public class ResultResponse<T> {

    private int code;

    private String msg;

    private T data;

    private Map<String, Object> ext;

    private static final int SUCCESS = 0;

    private static final String SUCCESS_MESSAGE = "SUCCESS";

    private ResultResponse() {
    }

    private ResultResponse(int code, String msg, T data, Map<String, Object> ext) {
        this.code = code;
        this.msg = msg;
        this.data = data;
        this.ext = ext;
    }

    public static <T> ResultResponse<T> success() {
        return new ResultResponse<>(SUCCESS, SUCCESS_MESSAGE, null, null);
    }

    public static <T> ResultResponse<T> success(T data) {
        return new ResultResponse<>(SUCCESS, SUCCESS_MESSAGE, data, null);
    }

    public static <T> ResultResponse<T> success(Map<String, Object> ext) {
        return new ResultResponse<>(SUCCESS, SUCCESS_MESSAGE, null, ext);
    }
}
