package com.walker.redis.excel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.fastjson.JSON;
import com.walker.redis.dto.CarButlerCacheDTO;
import com.walker.redis.dto.CarButlerDTO;
import com.walker.redis.dto.InsuranceDTO;
import com.walker.redis.dto.LocationDTO;
import com.walker.redis.util.DateUtil;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
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
public class CarButlerDTOExcelListener extends AnalysisEventListener<CarButlerDTO> {

    private static final int BATCH_COUNT = 3000;
    private StringRedisTemplate redisTemplate;
    private List<CarButlerDTO> dtos = new ArrayList<>();

    public CarButlerDTOExcelListener(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void invoke(CarButlerDTO carButlerDTO, AnalysisContext analysisContext) {
        Date date = carButlerDTO.getDate();
        if (DateUtil.toLocalDateTime(date).isBefore(LocalDateTime.of(2020, 7, 6, 12, 59, 59))) {
            dtos.add(carButlerDTO);
        }
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
        String key = "car:butler:province:real:time:order:310100";
        Map<String, String> params = new HashMap<>();
        if (!CollectionUtils.isEmpty(dtos)) {
            for (CarButlerDTO dto : dtos) {
                String k = DateUtil.localDateTimeToString(DateUtil.toLocalDateTime(dto.getDate()), DateUtil.TIME_PATTERN);
                Object val = redisTemplate.opsForHash().get(key, k);

                List<LocationDTO> locations = new ArrayList<>();
                LocationDTO locationDTO = new LocationDTO();
                String[] positions = dto.getPosition().split(",");
                locationDTO.setLongitude(BigDecimal.valueOf(Double.parseDouble(positions[0])));
                locationDTO.setLatitude(BigDecimal.valueOf(Double.parseDouble(positions[1])));

                if (val == null) {
                    locations.add(locationDTO);
                } else {
                    locations = JSON.parseArray(String.valueOf(val), LocationDTO.class);
                    locations.add(locationDTO);
                }
                params.put(k, JSON.toJSONString(locations));
            }
            redisTemplate.opsForHash().putAll(key, params);
        }
    }

    private void intoRedis() {
        // String key = "car:butler:province:cache:order:江苏";
        // String key = "car:butler:province:cache:order:上海";
        String key = "car:butler:province:cache:order:广东";
        // String key = "car:butler:province:cache:order:浙江";
        Map<String, String> params = new HashMap<>();
        if (!CollectionUtils.isEmpty(dtos)) {
            for (CarButlerDTO dto : dtos) {
                String k = DateUtil.localDateTimeToString(DateUtil.toLocalDateTime(dto.getDate()), DateUtil.NORMAL_PATTERN);
                CarButlerCacheDTO cacheDTO = new CarButlerCacheDTO();
                String[] positions = dto.getPosition().split(",");
                cacheDTO.setLongitude(BigDecimal.valueOf(Double.parseDouble(positions[0])));
                cacheDTO.setLatitude(BigDecimal.valueOf(Double.parseDouble(positions[1])));
                cacheDTO.setProvince("广东");
                cacheDTO.setCity(dto.getCity());
                cacheDTO.setTypeName(dto.getTypeName());
                cacheDTO.setRelaMerchantName(dto.getName());

                params.put(k, JSON.toJSONString(cacheDTO));
            }
            redisTemplate.opsForHash().putAll(key, params);
        }
    }
}
