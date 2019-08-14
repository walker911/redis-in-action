package com.walker.redis;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.walker.redis.dto.*;
import com.walker.redis.util.HttpClientUtil;
import org.apache.logging.log4j.util.Strings;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
public class RedisInActionApplicationTests {

    @Autowired
    private StringRedisTemplate redisTemplate;

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
                    trans();
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
                redisTemplate.opsForValue().increment("trans:", -1);
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
}
