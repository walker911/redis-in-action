package com.walker.redis.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author walker
 * @date 2019/8/14
 */
@Data
@AllArgsConstructor
public class OrderDTO {

    private String customer;

    private String province;

    private String time;

    private String node;
}
