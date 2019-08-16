package com.walker.redis.dto;

import lombok.Data;

/**
 * @author walker
 * @date 2019/8/16
 */
@Data
public class ProvinceOrderDTO {
    private String province;

    private Integer drive;

    private Integer transfer;

    private Integer other;
}
