package com.walker.redis.dto;

import lombok.Data;

import java.util.List;

/**
 * @author walker
 * @date 2019/9/2
 */
@Data
public class ProvinceOrderResponse {
    private List<ProvinceOrderDTO> orderData;
}
