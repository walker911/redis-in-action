package com.walker.redis.excel;

import com.alibaba.excel.context.AnalysisContext;
import com.alibaba.excel.event.AnalysisEventListener;
import com.alibaba.fastjson.JSON;
import com.walker.redis.dto.GeoDTO;
import com.walker.redis.dto.HotCityDTO;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author walker
 * @date 2019/10/16
 */
public class ReadExcelListener extends AnalysisEventListener<HotCityDTO> {

    private static final int BATCH_COUNT = 3000;
    List<HotCityDTO> cities = new ArrayList<>();
    List<GeoDTO> geos = new ArrayList<>();

    @Override
    public void invoke(HotCityDTO hotCityDTO, AnalysisContext analysisContext) {
        cities.add(hotCityDTO);
        if (cities.size() >= BATCH_COUNT) {
            handleHotCity();
            cities.clear();
        }
    }

    @Override
    public void doAfterAllAnalysed(AnalysisContext analysisContext) {
        try {
            saveFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void saveFile() throws IOException {
        System.out.println(geos.size());
        File file = new File("C:\\Users\\ThinkPad\\Desktop\\shanghai.json");
        FileUtils.writeStringToFile(file, JSON.toJSONString(geos), StandardCharsets.UTF_8);
    }

    private void handleHotCity() {
        List<GeoDTO> batches = cities.stream().filter(dto -> "上海".equals(dto.getCity()) && StringUtils.isNotBlank(dto.getLocation())).map(dto -> {
            String location = dto.getLocation();
            GeoDTO geo = new GeoDTO();
            geo.setValue(10);
            String[] locArr = location.split(",");
            Double[] geoCoord = new Double[locArr.length];
            for (int i = 0; i < locArr.length; i++) {
                geoCoord[i] = Double.parseDouble(locArr[i]);
            }
            geo.setGeoCoord(geoCoord);

            return geo;
        }).collect(Collectors.toList());

        geos.addAll(batches);
    }
}
