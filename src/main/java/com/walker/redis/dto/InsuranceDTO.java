package com.walker.redis.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.annotation.format.NumberFormat;
import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.util.Date;

/**
 * <p>
 *
 * </p>
 *
 * @author mu qin
 * @date 2020/7/1
 */
@Data
public class InsuranceDTO {

    private String carId;

    @JSONField(serialize = false)
    private Date date;

    private String cityCode;

}
