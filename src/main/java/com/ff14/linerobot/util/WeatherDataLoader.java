package com.ff14.linerobot.util;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

public class WeatherDataLoader {

    public static void main(String[] args) {
        String fileName = "ff14weather/weather";

        try (InputStream is = WeatherDataLoader.class.getClassLoader().getResourceAsStream(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

            List<String[]> lineItems = reader.lines()
                    .map(line -> line.split("\\s*,\\s*")) // 根據逗號分隔，並移除前後的空白
                    .collect(Collectors.toList());

            // 示範：打印讀取和處理後的數據
            for (String[] items : lineItems) {
                System.out.println("Zone: " + items[0]);
                // 循環顯示每個地區的天氣條件和概率
                for (int i = 1; i < items.length; i += 2) {
                    String weather = items[i];
                    String probability = (i + 1 < items.length) ? items[i + 1] : "100"; // 如果沒有概率值則默認為100
                    System.out.println("  Weather: " + weather + ", Probability: " + probability);
                }
                System.out.println(); // 添加一個空行以分隔不同的地區
            }

            // https://ffxiv.pf-n.co/cn/skywatcher
            // thanks lulu's Tool for this calculation

            // Get seconds since Jan 1st 1970
            long unixTimestamp = Instant.now().getEpochSecond();

            // Get Eorzean hours/days since (1 Eorzean hour = 175 seconds)
            long eorzeanHours = unixTimestamp / 175;
            long eorzeanDays = eorzeanHours / 24;

            // calculate when is this weather interval start, and convert to earth time
            long hoursAfterIntervalStart = (eorzeanHours % 24) - (((eorzeanHours % 24)/8) * 8);
            long epochTimeWhenIntervalStart = (eorzeanHours - hoursAfterIntervalStart) * 175;
            System.out.println(epochTimeWhenIntervalStart);
            Instant instant = Instant.ofEpochSecond(epochTimeWhenIntervalStart);
            ZoneId zoneId = ZoneId.of("Asia/Taipei");
            LocalDateTime localDateTime = instant.atZone(zoneId).toLocalDateTime();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedDateTime = localDateTime.format(formatter);
            System.out.println(formattedDateTime); // Output: 2021-06-29 12:13:51
            System.out.println("ET " + ((eorzeanHours - hoursAfterIntervalStart) % 24) + ":00" ); // EorzeanTime


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}