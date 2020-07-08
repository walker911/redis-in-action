package com.walker.redis.dto;

import lombok.Data;

import java.math.BigDecimal;

/**
 * <p>
 *
 * </p>
 *
 * @author mu qin
 * @date 2020/7/7
 */
@Data
public class CarButlerCacheDTO {

    private String province;

    private String typeName;

    private String relaMerchantName;

    private String hour;

    private String minute;

    private BigDecimal longitude;

    private BigDecimal latitude;

    private String city;

    private String area;

}
