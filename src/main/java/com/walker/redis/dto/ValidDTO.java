package com.walker.redis.dto;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Digits;

/**
 * <p>
 *
 * </p>
 *
 * @author mu qin
 * @date 2020/8/17
 */
@Getter
@Setter
public class ValidDTO {

    @Digits(integer = 4, fraction = 2, message = "格式错误")
    @DecimalMin(value = "0", inclusive = false, message = "金额错误")
    private Double amount;

}
