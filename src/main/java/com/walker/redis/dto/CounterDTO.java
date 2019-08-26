package com.walker.redis.dto;

import lombok.Data;

/**
 * @author walker
 * @date 2019/8/26
 */
@Data
public class CounterDTO {

    private Integer key;

    private Integer value;
}
