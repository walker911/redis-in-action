package com.walker.redis.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import lombok.Data;

/**
 * @author walker
 * @date 2019/9/4
 */
@Data
public class StoreDTO {

    @ExcelProperty(index = 0)
    private String province;

    @ExcelProperty(index = 1)
    private String name;
}
