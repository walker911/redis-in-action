package com.walker.redis.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @author walker
 * @date 2019/10/14
 */
@Data
public class HotCityDTO {

    @ExcelProperty(index = 0)
    private String orderNo;

    @ExcelProperty(index = 1)
    private String orderTime;

    @ExcelProperty(index = 2)
    private String type;

    @ExcelProperty(index = 3)
    private String city;

    @ExcelProperty(index = 4)
    private String location;
}
