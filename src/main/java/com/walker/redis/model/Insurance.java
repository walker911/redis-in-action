package com.walker.redis.model;

import lombok.Data;

/**
 * @author walker
 * @date 2019/8/14
 */
@Data
public class Insurance {

    private String carId;

    private String status;

    private String amount;

    private String cityCode;

    private String lastCompany;

    private String engine;

    private String lastCiEndTime;

    private String branName;

    private String vin;

    private String enrollDate;

    private String lastBiEndTime;
}
