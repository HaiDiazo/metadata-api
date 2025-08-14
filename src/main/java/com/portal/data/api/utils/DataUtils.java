package com.portal.data.api.utils;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class DataUtils {

    private static LocalDateTime convertEpochToInstant(Object epoch) {
        String stringEpoch = String.valueOf(epoch);
        Instant instant = Instant.ofEpochMilli(Long.parseLong(stringEpoch));
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    private static String formatDateTime(LocalDateTime localDateTime) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return localDateTime.format(dateTimeFormatter);
    }

    public static String convertEpochToDatetime(Object epoch) {
        LocalDateTime localDateTime = convertEpochToInstant(epoch);
        return formatDateTime(localDateTime);
    }

    public static Map<String, String> dateRange() {
        Map<String, String> dateMap = new HashMap<>();

        LocalDateTime endDatetime = LocalDateTime.now();
        LocalDateTime startDatetime = endDatetime.minusDays(30);

        dateMap.put("startDate", formatDateTime(startDatetime));
        dateMap.put("endDate", formatDateTime(endDatetime));
        return dateMap;
    }
}
