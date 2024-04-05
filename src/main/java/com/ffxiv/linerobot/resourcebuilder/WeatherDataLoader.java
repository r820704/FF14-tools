package com.ffxiv.linerobot.resourcebuilder;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ffxiv.linerobot.dto.weather.Rates;
import com.ffxiv.linerobot.dto.weather.TerritoryType;
import com.ffxiv.linerobot.dto.weather.Weather;
import com.ffxiv.linerobot.dto.weather.WeatherRateIndices;
import com.ffxiv.linerobot.util.FFXIVTimeUtil;
import com.ffxiv.linerobot.util.JsonUtil;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class WeatherDataLoader {

    public static void main(String[] args) {
        String fileName = "ffxiv/weather/weather";

        try (InputStream is = WeatherDataLoader.class.getClassLoader().getResourceAsStream(fileName);
             BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {

//            List<String[]> lineItems = reader.lines()
//                    .map(line -> line.split("\\s*,\\s*")) // 根據逗號分隔，並移除前後的空白
//                    .collect(Collectors.toList());
//
//
//            // 示範：打印讀取和處理後的數據
//            for (String[] items : lineItems) {
//                System.out.println("Zone: " + items[0]);
//                // 循環顯示每個地區的天氣條件和概率
//                for (int i = 1; i < items.length; i += 2) {
//                    String weather = items[i];
//                    String probability = (i + 1 < items.length) ? items[i + 1] : "100"; // 如果沒有概率值則默認為100
//                    System.out.println("  Weather: " + weather + ", Probability: " + probability);
//                }
//                System.out.println(); // 添加一個空行以分隔不同的地區
//            }

            // https://ffxiv.pf-n.co/cn/skywatcher
            // thanks lulu's Tool for this calculation

            // Get seconds since Jan 1st 1970
            long earthEpochSeconds = Instant.now().getEpochSecond();

            // Get Eorzean hours/days since (1 Eorzean hour = 175 seconds)
            long eorzeanEpochHours = (long) FFXIVTimeUtil.getEorzeanEpochHours(earthEpochSeconds);
            long eorzeanEpochDays = eorzeanEpochHours / 24;

            // calculate when is this weather interval start, and convert to earth time
            long hoursAfterIntervalStart = (eorzeanEpochHours % 24) - (((eorzeanEpochHours % 24) / 8) * 8);
            long earthEpochTimeWhenIntervalStart = (eorzeanEpochHours - hoursAfterIntervalStart) * 175;
            System.out.println(earthEpochTimeWhenIntervalStart);
            Instant instant = Instant.ofEpochSecond(earthEpochTimeWhenIntervalStart);
            ZoneId zoneId = ZoneId.of("Asia/Taipei");
            LocalDateTime localDateTime = instant.atZone(zoneId).toLocalDateTime();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formattedDateTime = localDateTime.format(formatter);
            System.out.println("interval start earth time: " + formattedDateTime); // Output: 2021-06-29 12:13:51
            System.out.println("interval start ET: " + ((eorzeanEpochHours - hoursAfterIntervalStart) % 24) + ":00"); // EorzeanTime
            System.out.println("Current Earth Time:" + Instant.ofEpochSecond(earthEpochSeconds).atZone(zoneId).toLocalDateTime().format(formatter) );
            System.out.println("Current ET: " + FFXIVTimeUtil.convertEarthTimeToEorzeanTime(earthEpochSeconds));

            // use internal start time to calculate weather
            ObjectMapper mapper = new ObjectMapper();
            List<WeatherRateIndices> weatherRateIndicesList =
                    JsonUtil.readJsonFileToList("src/main/resources/ffxiv/resourcedata/WeatherRateIndices.json", WeatherRateIndices.class, mapper);
            List<TerritoryType> territoryTypeList =
                    JsonUtil.readJsonFileToList("src/main/resources/ffxiv/resourcedata/TerritoryType.json", TerritoryType.class, mapper);
            List<Weather> weatherList =
                    JsonUtil.readJsonFileToList("src/main/resources/ffxiv/resourcedata/Weather.json", Weather.class, mapper);
            String targetPlace = "Limsa Lominsa";

            // get forecastTarget
            long timeChunk = (eorzeanEpochHours % 24) - (eorzeanEpochHours % 8);
            timeChunk = (timeChunk + 8) % 24;

            long seed = eorzeanEpochDays * 100 + timeChunk;

            // Do a little hashing
            long step1 = (seed << 11) ^ seed;
            long step2 = (step1 >>> 8) ^ step1;

            // Return a number between 0-99 inclusive
            long forecastTarget = step2 % 100;
            System.out.println(forecastTarget);


            TerritoryType targetTerritoryType  = territoryTypeList
                    .stream()
                    .filter(territoryType -> territoryType.getPlaceName() != null &&
                            StringUtils.equals(StringUtils.trim(territoryType.getPlaceName().getNameEn()), targetPlace))
                    .findFirst().orElse(null);

            WeatherRateIndices targetWeatherRateIndices = weatherRateIndicesList
                    .stream()
                    .filter(weatherRateIndices -> weatherRateIndices.getId() == targetTerritoryType.getWeatherRateId())
                    .findFirst().orElse(null);

            Rates targetRate = targetWeatherRateIndices.getRates()
                    .stream()
                    .filter(rate -> forecastTarget < rate.getRate())
                    .findFirst().orElse(null);

            Weather targetWeather = weatherList
                    .stream()
                    .filter(weather -> weather.getId() == targetRate.getWeather())
                    .findFirst()
                    .orElse(null);

            System.out.println(targetTerritoryType.getPlaceName().getNameChs()+ ":"+ targetWeather.getNameChs());


        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}