package com.walker.redis.dto;

import lombok.Data;

/**
 * @author walker
 * @date 2019/9/6
 */
@Data
public class EmailDTO {

    private String seller;

    private String item;

    private String price;

    private String buyer;

    private Long time;
}
