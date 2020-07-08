package com.walker.redis.excel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.fastjson.JSON;
import com.walker.redis.dto.InsuranceDTO;
import com.walker.redis.util.DateUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p>
 *
 * </p>
 *
 * @author mu qin
 * @date 2020/7/1
 */
public class InsuranceDTOExcelListener extends AnalysisEventListener<InsuranceDTO> {

    private static final int BATCH_COUNT = 3000;
    List<InsuranceDTO> dtos = new ArrayList<>();
    private StringRedisTemplate redisTemplate;

    public InsuranceDTOExcelListener(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void invoke(InsuranceDTO insuranceDTO, AnalysisContext analysisContext) {
        insuranceDTO.setCityCode(insuranceDTO.getCityCode().substring(0, 6));
        dtos.add(insuranceDTO);
        if (dtos.size() >= BATCH_COUNT) {
            intoRedis();
            dtos.clear();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        intoRedis();
    }

    private void saveIntoRedis() {
        String key = "insurance:sub:real:time:310100";
        Map<String, String> params = new HashMap<>();
        if (!CollectionUtils.isEmpty(dtos)) {
            for (InsuranceDTO dto : dtos) {
                String k = DateUtil.localDateTimeToString(DateUtil.toLocalDateTime(dto.getDate()), DateUtil.TIME_PATTERN);
                Object val = redisTemplate.opsForHash().get(key, k);
                List<String> carIds = new ArrayList<>();
                if (val == null) {
                    carIds.add(dto.getCarId());
                } else {
                    carIds = JSON.parseArray(String.valueOf(val), String.class);
                    carIds.add(dto.getCarId());
                }
                params.put(k, JSON.toJSONString(carIds));
            }
            redisTemplate.opsForHash().putAll(key, params);
        }
    }

    private void intoRedis() {
        String key = "insurance:sub:real:time:310100";
        Map<String, String> params = new HashMap<>();
        if (!CollectionUtils.isEmpty(dtos)) {
            for (InsuranceDTO dto : dtos) {
                String k = DateUtil.localDateTimeToString(DateUtil.toLocalDateTime(dto.getDate()), DateUtil.NORMAL_PATTERN);
                params.put(k, JSON.toJSONString(dto));
            }
            redisTemplate.opsForHash().putAll(key, params);
        }
    }
}
