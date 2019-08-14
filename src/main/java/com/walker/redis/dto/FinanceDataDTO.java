package com.walker.redis.dto;

import lombok.Data;

/**
 * @author walker
 * @date 2019/8/14
 */
@Data
public class FinanceDataDTO {
    private String code;

    private String msg;

    private FinanceDTO data;
}
