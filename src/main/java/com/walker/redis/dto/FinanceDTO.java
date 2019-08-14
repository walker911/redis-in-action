package com.walker.redis.dto;

import lombok.Data;

import java.util.List;

/**
 * @author walker
 * @date 2019/8/14
 */
@Data
public class FinanceDTO {

    private String allTotalCount;

    private String currentMonthBuyNum;

    private String currentMonthPayNum;

    private String currentYearBuyNum;

    private String currentYearPayNum;

    private List<RegionalRankDTO> regionalRank;

    private List<YearBuyByMonthDTO> yearBuyByMonthJsonArr;
}
