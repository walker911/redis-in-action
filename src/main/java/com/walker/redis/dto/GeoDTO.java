package com.walker.redis.dto;

import lombok.Data;

/**
 * @author walker
 * @date 2019/10/14
 */
@Data
public class GeoDTO {

    private Integer value;

    private Double[] geoCoord;
}
