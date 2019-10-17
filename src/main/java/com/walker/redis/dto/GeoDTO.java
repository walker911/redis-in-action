package com.walker.redis.dto;

import lombok.Data;

import java.util.Arrays;

/**
 * @author walker
 * @date 2019/10/14
 */
@Data
public class GeoDTO {

    private Integer value;

    private Double[] geoCoord;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeoDTO geoDTO = (GeoDTO) o;
        return Arrays.equals(geoCoord, geoDTO.geoCoord);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(geoCoord);
    }
}
