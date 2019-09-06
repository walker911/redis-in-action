package com.walker.redis;

import com.alibaba.excel.EasyExcel;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.walker.redis.cache.SemaphoreRedis;
import com.walker.redis.dto.*;
import com.walker.redis.util.HttpClientUtil;
import org.apache.logging.log4j.util.Strings;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisInActionApplicationTests {

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private SemaphoreRedis semaphoreRedis;

    @Test
    public void contextLoads() {
        Boolean result = redisTemplate.opsForZSet().add("time:", "article:3", System.currentTimeMillis());
        System.out.println(result);
    }

    /**
     * 测试没有事务
     *
     * @throws InterruptedException
     */
    @Test
    public void noTransTest() throws InterruptedException {
        for (int i = 0; i < 3; i++) {
            Thread thread = new Thread(() -> {
                try {
                    noTrans();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            });
            thread.start();
            TimeUnit.MILLISECONDS.sleep(500);
        }
    }

    private void noTrans() throws InterruptedException {
        System.out.println(redisTemplate.opsForValue().increment("notrans:"));
        TimeUnit.MILLISECONDS.sleep(100);
        redisTemplate.opsForValue().increment("notrans:", -1);
    }

    /**
     * 测试有事务
     *
     * @throws InterruptedException
     */
    @Test
    public void transTest() throws InterruptedException {
        for (int i = 0; i < 3; i++) {
            Thread thread = new Thread(() -> {
                try {
                    sessionCallback();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
            thread.start();
            TimeUnit.MILLISECONDS.sleep(500);
        }
    }

    private void trans() throws InterruptedException {
        // 开启事务
        redisTemplate.setEnableTransactionSupport(true);

        redisTemplate.multi();
        redisTemplate.opsForValue().increment("trans:");
        TimeUnit.MILLISECONDS.sleep(100);
        redisTemplate.opsForValue().increment("trans:", -1);
        System.out.println(redisTemplate.exec().get(0));
    }

    @SuppressWarnings("unchecked")
    private void sessionCallback() {
        SessionCallback<Object> callback = new SessionCallback<Object>() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                operations.multi();
                operations.opsForValue().increment("trans:");
                try {
                    TimeUnit.MILLISECONDS.sleep(100);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                operations.opsForValue().increment("trans:", -1);
                return operations.exec();
            }
        };

        System.out.println(redisTemplate.execute(callback));
    }

    @Test
    public void yearTotal() {
        Map<String, String> params = new HashMap<>();
        String[] months = {"1月", "2月", "3月", "4月", "5月", "6月", "7月", "8月", "9月", "10月", "11月", "12月"};
        String[] nums = {"82", "102", "112", "150", "180", "192", "210", "253", "262", "289", "310", "345"};
        params.put("num", "52323180");
        params.put("money", "893724678");
        params.put("months", Strings.join(Arrays.asList(months), '|'));
        params.put("nums", Strings.join(Arrays.asList(nums), '|'));
        redisTemplate.opsForHash().putAll("finance:year:total", params);
        // redisTemplate.opsForList().rightPushAll("finance:year:total:months", months);
        // redisTemplate.opsForList().rightPushAll("finance:year:total:nums", nums);
    }

    @Test
    public void orderTotal() {
        redisTemplate.opsForValue().set("finance:order:total", "293724678");
        List<String> realTimeOrders = new ArrayList<>();
        OrderDTO orderDTO1 = new OrderDTO("吉冠*", "上海市", "10:55", "风控复审通过");
        OrderDTO orderDTO2 = new OrderDTO("吉冠*", "上海市", "10:56", "风控复审通过");
        OrderDTO orderDTO3 = new OrderDTO("吉冠*", "上海市", "10:57", "风控复审通过");
        OrderDTO orderDTO4 = new OrderDTO("吉冠*", "上海市", "10:58", "风控复审通过");
        OrderDTO orderDTO5 = new OrderDTO("吉冠*", "上海市", "10:59", "风控复审通过");
        OrderDTO orderDTO6 = new OrderDTO("吉冠*", "上海市", "11:01", "风控复审通过");
        OrderDTO orderDTO7 = new OrderDTO("吉冠*", "上海市", "11:02", "风控复审通过");
        OrderDTO orderDTO8 = new OrderDTO("吉冠*", "上海市", "11:03", "风控复审通过");
        OrderDTO orderDTO9 = new OrderDTO("吉冠*", "上海市", "11:04", "风控复审通过");
        OrderDTO orderDTO10 = new OrderDTO("吉冠*", "上海市", "11:05", "风控复审通过");
        realTimeOrders.add(JSON.toJSONString(orderDTO1));
        realTimeOrders.add(JSON.toJSONString(orderDTO2));
        realTimeOrders.add(JSON.toJSONString(orderDTO3));
        realTimeOrders.add(JSON.toJSONString(orderDTO4));
        realTimeOrders.add(JSON.toJSONString(orderDTO5));
        realTimeOrders.add(JSON.toJSONString(orderDTO6));
        realTimeOrders.add(JSON.toJSONString(orderDTO7));
        realTimeOrders.add(JSON.toJSONString(orderDTO8));
        realTimeOrders.add(JSON.toJSONString(orderDTO9));
        realTimeOrders.add(JSON.toJSONString(orderDTO10));
        redisTemplate.opsForList().rightPushAll("finance:order:real:time", realTimeOrders);

    }

    @Test
    public void fetchData() throws IOException {
        String result = HttpClientUtil.doPost("http://:8080/iFinCarGateway/getScreenData");
        FinanceDataDTO dataDTO = JSON.parseObject(result, FinanceDataDTO.class);
        System.out.println(JSON.toJSONString(dataDTO));
        // 当月
        Map<String, String> monthParams = new HashMap<>();
        Long currentMonthBuyNum = Long.parseLong(dataDTO.getData().getCurrentMonthBuyNum()) * 6;
        Long currentMonthPayNum = Long.parseLong(dataDTO.getData().getCurrentMonthPayNum()) * 6;
        monthParams.put("num", String.valueOf(currentMonthBuyNum));
        monthParams.put("money", String.valueOf(currentMonthPayNum));
        redisTemplate.opsForHash().putAll("finance:month:total", monthParams);
        // 当年
        Map<String, String> yearParams = new HashMap<>();
        Long currentYearBuyNum = Long.parseLong(dataDTO.getData().getCurrentYearBuyNum()) * 6;
        Long currentYearPayNum = Long.parseLong(dataDTO.getData().getCurrentYearPayNum()) * 6;
        yearParams.put("num", String.valueOf(currentYearBuyNum));
        yearParams.put("money", String.valueOf(currentYearPayNum));
        // 1月 - 取上年12个月, 否则, 取当年当月之前的月份
        List<YearBuyByMonthDTO> yearBuyByMonths = dataDTO.getData().getYearBuyByMonthJsonArr();
        yearBuyByMonths.forEach(buyMonth -> {
            Long num = Long.parseLong(buyMonth.getNum()) * 6;
            buyMonth.setNum(String.valueOf(num));
        });
        Collections.reverse(yearBuyByMonths);

        List<YearBuyByMonthDTO> yearBuyByMonthDTOList;
        int month = LocalDate.now().getMonthValue();
        if (month > 1) {
            yearBuyByMonthDTOList = yearBuyByMonths.subList(0, month - 1);
        } else {
            yearBuyByMonthDTOList = yearBuyByMonths.subList(0, 12);
        }

        List<String> months = new ArrayList<>();
        List<String> nums = new ArrayList<>();
        Collections.reverse(yearBuyByMonthDTOList);
        for (int i = 0; i < yearBuyByMonthDTOList.size(); i++) {
            YearBuyByMonthDTO buyByMonthDTO = yearBuyByMonthDTOList.get(i);
            months.add(String.format("%d月", i + 1));
            nums.add(buyByMonthDTO.getNum());
        }
        yearParams.put("months", JSON.toJSONString(months));
        yearParams.put("nums", JSON.toJSONString(nums));

        redisTemplate.opsForHash().putAll("finance:year:total", yearParams);
        // 累计订单
        Long allTotalCount = Long.parseLong(dataDTO.getData().getAllTotalCount()) * 6;
        redisTemplate.opsForValue().set("finance:order:total", String.valueOf(allTotalCount));
        // Hot区域
        List<RegionalRankDTO> ranks = dataDTO.getData().getRegionalRank();
        ranks.forEach(rank -> {
            Long num = Long.parseLong(rank.getNum()) * 6;
            rank.setNum(String.valueOf(num));
        });
        redisTemplate.opsForValue().set("finance:hot:area:rank", JSON.toJSONString(ranks));
    }

    @Test
    public void realTimeOrder() {
        JSONObject object = new JSONObject();
        object.put("order_time", "16:08");
        object.put("order_province", "上海市");
        object.put("order_customer", "张三");
        object.put("order_node", "退回");
        System.out.println(object.toString());
        Finance finance = JSON.parseObject(object.toString(), Finance.class);
        System.out.println(JSON.toJSONString(finance));
    }

    @Test
    public void test() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm:ss");
        System.out.println(LocalTime.now().format(formatter));
    }

    @Test
    public void provinceOrder() {
        String json = "{\"orderData\":[{\"driveNum\":11,\"otherNum\":1,\"province\":\"山东\",\"transferNum\":1},{\"driveNum\":8,\"otherNum\":1,\"province\":\"福建\",\"transferNum\":2},{\"driveNum\":4,\"otherNum\":3,\"province\":\"河北\",\"transferNum\":8},{\"driveNum\":20,\"otherNum\":3,\"province\":\"河南\",\"transferNum\":2},{\"driveNum\":3,\"otherNum\":2,\"province\":\"重庆\",\"transferNum\":5},{\"driveNum\":9,\"otherNum\":4,\"province\":\"湖北\",\"transferNum\":0},{\"driveNum\":8,\"otherNum\":0,\"province\":\"湖南\",\"transferNum\":0},{\"driveNum\":1,\"otherNum\":0,\"province\":\"江西\",\"transferNum\":0},{\"driveNum\":5,\"otherNum\":0,\"province\":\"海南\",\"transferNum\":4},{\"driveNum\":0,\"otherNum\":0,\"province\":\"黑龙江\",\"transferNum\":8},{\"driveNum\":1,\"otherNum\":0,\"province\":\"天津\",\"transferNum\":2},{\"driveNum\":0,\"otherNum\":0,\"province\":\"陕西\",\"transferNum\":5},{\"driveNum\":1,\"otherNum\":0,\"province\":\"贵州\",\"transferNum\":0},{\"driveNum\":0,\"otherNum\":0,\"province\":\"新疆\",\"transferNum\":1},{\"driveNum\":10,\"otherNum\":7,\"province\":\"江苏\",\"transferNum\":17},{\"driveNum\":3,\"otherNum\":1,\"province\":\"安徽\",\"transferNum\":4},{\"driveNum\":0,\"otherNum\":0,\"province\":\"西藏\",\"transferNum\":1},{\"driveNum\":2,\"otherNum\":0,\"province\":\"吉林\",\"transferNum\":12},{\"driveNum\":51,\"otherNum\":21,\"province\":\"上海\",\"transferNum\":150},{\"driveNum\":0,\"otherNum\":0,\"province\":\"宁夏\",\"transferNum\":3},{\"driveNum\":0,\"otherNum\":0,\"province\":\"甘肃\",\"transferNum\":1},{\"driveNum\":3,\"otherNum\":0,\"province\":\"山西\",\"transferNum\":0},{\"driveNum\":14,\"otherNum\":1,\"province\":\"四川\",\"transferNum\":21},{\"driveNum\":2,\"otherNum\":1,\"province\":\"广西\",\"transferNum\":0},{\"driveNum\":4,\"otherNum\":5,\"province\":\"浙江\",\"transferNum\":11},{\"driveNum\":2,\"otherNum\":0,\"province\":\"云南\",\"transferNum\":2},{\"driveNum\":22,\"otherNum\":0,\"province\":\"内蒙古\",\"transferNum\":1},{\"driveNum\":2,\"otherNum\":0,\"province\":\"辽宁\",\"transferNum\":19},{\"driveNum\":31,\"otherNum\":5,\"province\":\"广东\",\"transferNum\":23},{\"driveNum\":3,\"otherNum\":4,\"province\":\"北京\",\"transferNum\":21}]}";
        ProvinceOrderResponse response = JSON.parseObject(json, ProvinceOrderResponse.class);

        Map<String, String> params = new HashMap<>();
        response.getOrderData().forEach(dto -> {
            List<ProvinceOrderNumDTO> numDTOS = new ArrayList<>();
            ProvinceOrderNumDTO numDTO = new ProvinceOrderNumDTO();
            BeanUtils.copyProperties(dto, numDTO);
            numDTOS.add(numDTO);
            params.put(dto.getProvince(), JSON.toJSONString(numDTOS));
        });

        redisTemplate.opsForHash().putAll("car:butler:province:order", params);
    }

    @Test
    public void dayOrder() {
        String json = "[{\"hour\":2019082210,\"num\":100},{\"hour\":2019082209,\"num\":200}," +
                "{\"hour\":2019082208,\"num\":250},{\"hour\":2019082207,\"num\":300}," +
                "{\"hour\":2019082206,\"num\":350},{\"hour\":2019082205,\"num\":400}," +
                "{\"hour\":2019082204,\"num\":430},{\"hour\":2019082203,\"num\":340}," +
                "{\"hour\":2019082202,\"num\":542},{\"hour\":2019082201,\"num\":290}," +
                "{\"hour\":2019082200,\"num\":390},{\"hour\":2019082123,\"num\":460}," +
                "{\"hour\":2019082122,\"num\":560},{\"hour\":2019082121,\"num\":510}," +
                "{\"hour\":2019082120,\"num\":670},{\"hour\":2019082119,\"num\":320}," +
                "{\"hour\":2019082118,\"num\":312},{\"hour\":2019082117,\"num\":345}," +
                "{\"hour\":2019082116,\"num\":323},{\"hour\":2019082115,\"num\":421}," +
                "{\"hour\":2019082114,\"num\":126},{\"hour\":2019082113,\"num\":230}," +
                "{\"hour\":2019082112,\"num\":280},{\"hour\":2019082111,\"num\":240}," +
                "{\"hour\":2019082110,\"num\":180}]";

        List<DayOrderDTO> dtos = JSON.parseArray(json, DayOrderDTO.class);
        Map<String, String> params = dtos.stream().collect(Collectors.toMap(DayOrderDTO::getHour, DayOrderDTO::getNum));

        redisTemplate.opsForHash().putAll("car:butler:day:order", params);
    }

    @Test
    public void readStoreExcel() {
        Map<String, Integer> provinces = new HashMap<>();
        provinces.put("河北", 0);
        provinces.put("内蒙古", 1);
        provinces.put("浙江", 2);
        provinces.put("辽宁", 3);
        provinces.put("山东", 4);
        provinces.put("广东", 5);
        provinces.put("广西", 6);
        provinces.put("湖南", 7);
        provinces.put("湖北", 8);
        provinces.put("江苏", 9);
        provinces.put("贵州", 10);
        provinces.put("河南", 11);
        provinces.put("山西", 12);
        provinces.put("西藏", 13);
        provinces.put("吉林", 14);
        provinces.put("福建", 15);
        provinces.put("安徽", 16);
        provinces.put("宁夏", 17);
        provinces.put("甘肃", 18);
        provinces.put("江西", 19);
        provinces.put("四川", 20);
        provinces.put("新疆", 21);
        provinces.put("陕西", 22);
        provinces.put("青海", 23);
        provinces.put("云南", 23);
        provinces.put("上海", 25);
        provinces.put("北京", 26);
        provinces.put("黑龙江", 27);
        provinces.put("天津", 28);
        provinces.put("海南", 29);
        provinces.put("重庆", 30);
        String path = "E:\\project\\redis-in-action\\src\\main\\resources\\网点导出列表.xlsx";
        // 读取Excel
        List<Object> items = EasyExcel.read(path).head(StoreDTO.class).sheet().doReadSync();
        for (Object item : items) {
            StoreDTO dto = (StoreDTO) item;
            int suffix = provinces.get(dto.getProvince());
            String key = "car:butler:virtual:store:" + suffix;
            redisTemplate.opsForList().leftPush(key, dto.getName());
        }
    }

    @Test
    public void semaphoreTest() {
        semaphoreRedis.acquireSemaphore("semaphore", 10, 10000);
    }
}
