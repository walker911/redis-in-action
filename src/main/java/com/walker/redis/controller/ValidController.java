package com.walker.redis.controller;

import com.walker.redis.common.ResultResponse;
import com.walker.redis.dto.ValidDTO;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * <p>
 *
 * </p>
 *
 * @author mu qin
 * @date 2020/8/17
 */
@RestController
public class ValidController {

    @PostMapping(value = "/valid")
    public ResultResponse<String> valid(@Valid @RequestBody ValidDTO dto) {
        return ResultResponse.success();
    }
}
