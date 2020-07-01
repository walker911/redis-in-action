package com.walker.redis.util;

import lombok.extern.slf4j.Slf4j;

import java.text.ParseException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author walker
 * @date 2019/9/11
 */
@Slf4j
public class DateUtil {

    public static final String NORMAL_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final String DATE_PATTERN = "yyyy-MM-dd";
    public static final String TIME_PATTERN = "HH:mm:ss";

    public static Date parseDate(String date, String pattern) {
        LocalDateTime dateTime = LocalDateTime.parse(date, DateTimeFormatter.ofPattern(pattern));
        ZonedDateTime zonedDateTime = dateTime.atZone(ZoneId.systemDefault());
        return Date.from(zonedDateTime.toInstant());
    }

    public static List<String> getLastTwelveMonth() {
        List<String> months = new ArrayList<>();

        LocalDate last = LocalDate.now().minusMonths(12);
        for (int i = 0; i < 12; i++) {
            String date = last.plusMonths(i).format(DateTimeFormatter.ofPattern("yyyy/MM"));
            months.add(date);
        }

        return months;
    }

    public static List<String> getTwelveMonth() {
        List<String> months = new ArrayList<>();

        LocalDate last = LocalDate.now().minusMonths(12);
        for (int i = 0; i < 12; i++) {
            String date = last.plusMonths(i).format(DateTimeFormatter.ofPattern("yy/MM"));
            months.add(date);
        }

        return months;
    }

    /**
     * 获取某天开始时间00:00的时间戳
     *
     * @param date
     * @return
     */
    public static long getStartTimeOfDay(LocalDate date) {
        LocalDateTime startDateTime = LocalDateTime.of(date, LocalTime.MIN);
        return startDateTime.toInstant(ZoneOffset.of("+8")).toEpochMilli();
    }

    public static Date getStartDateOfDay(LocalDate date) {
        LocalDateTime startDateTime = LocalDateTime.of(date, LocalTime.MIN);
        ZonedDateTime zonedDateTime = startDateTime.atZone(ZoneId.systemDefault());
        return Date.from(zonedDateTime.toInstant());
    }

    public static String localDateTimeToString(LocalDateTime dateTime, String pattern) {
        return DateTimeFormatter.ofPattern(pattern).format(dateTime);
    }

    public static List<Object> getLastTenDays() {
        List<Object> days = new ArrayList<>();
        LocalDateTime now = LocalDateTime.now();
        for (int i = 0; i < 10; i++) {
            LocalDateTime ldt = now.minusDays(i);
            String day = localDateTimeToString(ldt, NORMAL_PATTERN);
            days.add(day);
        }
        return days;
    }

    public static LocalDateTime toLocalDateTime(Date date) {
        return date.toInstant().atZone(ZoneId.of("GMT+8")).toLocalDateTime();
    }

    public static void main(String[] args) throws ParseException {
        System.out.println(getLastTenDays());
    }
}
