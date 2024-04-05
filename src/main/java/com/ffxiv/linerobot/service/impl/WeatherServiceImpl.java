package com.ffxiv.linerobot.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ffxiv.linerobot.dto.weather.*;
import com.ffxiv.linerobot.entity.BotConversationConfig;
import com.ffxiv.linerobot.repository.BotConversationConfigRepository;
import com.ffxiv.linerobot.service.WeatherService;
import com.ffxiv.linerobot.util.FFXIVTimeUtil;
import com.ffxiv.linerobot.util.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@Slf4j
public class WeatherServiceImpl implements WeatherService {

    private static final Map<String, List<WeatherCondition>> weatherData = new HashMap<>();
    private static final int DEFAULT_WEATHER_COUNT = 10;
    private static final DateTimeFormatter YYYY_MM_DD = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static List<WeatherRateIndices> weatherRateIndicesList = new ArrayList<>();
    private static List<TerritoryType> territoryTypeList = new ArrayList<>();
    private static List<Weather> weatherList = new ArrayList<>();
    @Autowired
    BotConversationConfigRepository botConversationConfigRepository;

    @PostConstruct
    public void loadWeatherData() throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        List<WeatherRateIndices> loadedWeatherRateIndicesList =
                JsonUtil.readJsonFileToList("src/main/resources/ffxiv/resourcedata/WeatherRateIndices.json", WeatherRateIndices.class, mapper);
        weatherRateIndicesList = Collections.unmodifiableList(loadedWeatherRateIndicesList);

        List<TerritoryType> loadedTerritoryTypeList =
                JsonUtil.readJsonFileToList("src/main/resources/ffxiv/resourcedata/TerritoryType.json", TerritoryType.class, mapper);
        territoryTypeList = Collections.unmodifiableList(loadedTerritoryTypeList);

        List<Weather> loadedWeatherList =
                JsonUtil.readJsonFileToList("src/main/resources/ffxiv/resourcedata/Weather.json", Weather.class, mapper);
        weatherList = Collections.unmodifiableList(loadedWeatherList);
    }

    @Override
    public List<WeatherConversationResult> getWeatherProbability(String userInputParam) {

        BotConversationConfig botConversationConfig =
                botConversationConfigRepository
                        .findById(BotConversationConfig.builder().topic("weather").conversationId(userInputParam).build())
                        .orElse(null);
        if (botConversationConfig == null) {
            throw new RuntimeException("無此選項");
        }

        String targetPlaceNameEn = botConversationConfig.getDetail();

        // Get seconds since Jan 1st 1970
        long earthEpochSeconds = Instant.now().getEpochSecond();
        // Get Eorzean hours/days since (1 Eorzean hour = 175 seconds)
        long eorzeanEpochHours = (long) FFXIVTimeUtil.getEorzeanEpochHours(earthEpochSeconds);
        // calculate when is this weather interval start, and convert to earth time
        long earthEpochSecondsWhenEorzeanWeatherIntervalStart = getEarthEpochTimeWhenEorzeaWeatherIntervalStart(eorzeanEpochHours);
        long eorzeanEpochHoursWhenEorzeanWeatherIntervalStart = (long) FFXIVTimeUtil.getEorzeanEpochHours(earthEpochSecondsWhenEorzeanWeatherIntervalStart);

        log.info("earth epochSeconds when eorzean weather interval start:" + earthEpochSecondsWhenEorzeanWeatherIntervalStart);
        LocalDateTime localDateTimeWhenWeatherIntervalStart =
                FFXIVTimeUtil.getZoneEarthTime(earthEpochSecondsWhenEorzeanWeatherIntervalStart, ZoneId.of("Asia/Taipei"));

        String formattedDateTime = localDateTimeWhenWeatherIntervalStart.format(YYYY_MM_DD);
        log.info("interval start earth time: " + formattedDateTime); // Output: 2021-06-29 12:13:51
        log.info("interval start ET: " + FFXIVTimeUtil.convertEarthTimeToEorzeanTime(earthEpochSecondsWhenEorzeanWeatherIntervalStart)); // EorzeanTime

        List<WeatherConversationResult> weatherConversationResultList = new ArrayList<>();
        long targetEorzeanEpochHours = eorzeanEpochHoursWhenEorzeanWeatherIntervalStart;
        for (int i = 1; i <= DEFAULT_WEATHER_COUNT; i++) {
            long targetEarthEpochSeconds = getEarthEpochTimeWhenEorzeaWeatherIntervalStart(targetEorzeanEpochHours);

            int forecastTarget = getForecastTarget(targetEorzeanEpochHours);
            Weather targetWeather = getTargetWeather(targetPlaceNameEn, forecastTarget);

            List<WeatherConversationResultDetail> weatherConversationResultDetailList = null;
            if (weatherConversationResultList.isEmpty()) {
                WeatherConversationResult weatherConversationResult = new WeatherConversationResult();
                weatherConversationResult.setPlaceName(targetWeather.getNameChs());
                weatherConversationResultDetailList = new ArrayList<>();
                weatherConversationResult.setDetails(weatherConversationResultDetailList);
                weatherConversationResultList.add(weatherConversationResult);
            }else {
                weatherConversationResultDetailList = Objects.requireNonNull(weatherConversationResultList
                        .stream()
                        .filter(result -> StringUtils.equals(targetWeather.getNameChs(), result.getPlaceName()))
                        .findFirst()
                        .orElse(null))
                        .getDetails();
            }
            WeatherConversationResultDetail weatherConversationResultDetail = new WeatherConversationResultDetail();
            weatherConversationResultDetail.setWeatherName(targetWeather.getNameChs());
            weatherConversationResultDetail.setEarthTime(FFXIVTimeUtil.getZoneEarthTime(targetEarthEpochSeconds, ZoneId.of("Asia/Taipei")));
            weatherConversationResultDetail.setEorzeanTime(FFXIVTimeUtil.convertEarthTimeToEorzeanTime(targetEarthEpochSeconds));

            weatherConversationResultDetailList.add(weatherConversationResultDetail);
            targetEorzeanEpochHours = targetEorzeanEpochHours + 8;
        }

        return weatherConversationResultList;
    }

    private Weather getTargetWeather(String targetPlaceNameEn, int forecastTarget) {
        TerritoryType targetTerritoryType = territoryTypeList
                .stream()
                .filter(territoryType -> territoryType.getPlaceName() != null &&
                        StringUtils.equals(StringUtils.trim(territoryType.getPlaceName().getNameEn()), targetPlaceNameEn))
                .findFirst().orElse(null);

        WeatherRateIndices targetWeatherRateIndices = weatherRateIndicesList
                .stream()
                .filter(weatherRateIndices -> weatherRateIndices.getId() == targetTerritoryType.getWeatherRateId())
                .findFirst().orElse(null);

        Rates targetRate = targetWeatherRateIndices.getRates()
                .stream()
                .filter(rate -> forecastTarget < rate.getRate())
                .findFirst().orElse(null);

        return weatherList
                .stream()
                .filter(weather -> weather.getId() == targetRate.getWeather())
                .findFirst()
                .orElse(null);
    }

    // Eorzean Weather will change 3 times in One Eorzean Day, ET 00:00、ET 08:00、ET 16:00
    // get current Weather Start time in eorzean time
    public long getEarthEpochTimeWhenEorzeaWeatherIntervalStart(long eorzeanEpochHours) {

        long hoursAfterIntervalStart = (eorzeanEpochHours % 24) - (((eorzeanEpochHours % 24) / 8) * 8);

        return (eorzeanEpochHours - hoursAfterIntervalStart) * 175;
    }

    // https://github.com/xivapi/ffxiv-datamining/blob/master/docs/Weather.md
    public int getForecastTarget(long eorzeanEpochHours) {
        long eorzeanEpochDays = eorzeanEpochHours / 24;
        long timeChunk = (eorzeanEpochHours % 24) - (eorzeanEpochHours % 8);
        timeChunk = (timeChunk + 8) % 24;

        long seed = eorzeanEpochDays * 100 + timeChunk;

        // Do a little hashing
        long step1 = (seed << 11) ^ seed;
        long step2 = (step1 >>> 8) ^ step1;

        // Return a number between 0-99 inclusive
        int forecastTarget = (int) (step2 % 100);
        return forecastTarget;
    }

}
