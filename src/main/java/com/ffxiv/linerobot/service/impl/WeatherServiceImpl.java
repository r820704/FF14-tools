package com.ffxiv.linerobot.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ffxiv.linerobot.dto.weather.*;
import com.ffxiv.linerobot.entity.BotConversationConfig;
import com.ffxiv.linerobot.entity.pk.BotConversationConfigPrimaryKey;
import com.ffxiv.linerobot.repository.BotConversationConfigRepository;
import com.ffxiv.linerobot.service.WeatherService;
import com.ffxiv.linerobot.util.FFXIVTimeUtil;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import javax.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class WeatherServiceImpl implements WeatherService {

  private static final int DEFAULT_WEATHER_COUNT = 10;
  private static final DateTimeFormatter YYYY_MM_DD =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
  public static List<WeatherRateIndices> weatherRateIndicesList = new ArrayList<>();
  public static List<TerritoryType> territoryTypeList = new ArrayList<>();
  public static List<Weather> weatherList = new ArrayList<>();
  @Autowired BotConversationConfigRepository botConversationConfigRepository;

  private static TerritoryType getTerritoryType(String targetPlaceNameEn) {
    TerritoryType targetTerritoryType =
        territoryTypeList.stream()
            .filter(
                territoryType ->
                    territoryType.getPlaceName() != null
                        && territoryType.getWeatherRateId()
                            != 0 // WeatherRateId=0的資料都不是真的被拿來使用的資料，例: 利姆萨·罗敏萨 & 利姆萨·罗敏萨上层甲板
                        && (StringUtils.equals(
                                StringUtils.trim(territoryType.getPlaceName().getNameEn()),
                                targetPlaceNameEn)
                            || StringUtils.contains(
                                StringUtils.trim(territoryType.getPlaceName().getNameEn()),
                                targetPlaceNameEn)))
            .findFirst()
            .orElse(null);
    return targetTerritoryType;
  }

  @PostConstruct
  public void loadWeatherData() throws IOException {
    ObjectMapper mapper = new ObjectMapper();

    // 使用ClassLoader來讀取資源文件
    try (InputStream weatherRateIndicesStream =
        getClass()
            .getClassLoader()
            .getResourceAsStream("ffxiv/resourcedata/WeatherRateIndices.json")) {
      List<WeatherRateIndices> loadedWeatherRateIndicesList =
          mapper.readValue(
              weatherRateIndicesStream, new TypeReference<List<WeatherRateIndices>>() {});
      weatherRateIndicesList = Collections.unmodifiableList(loadedWeatherRateIndicesList);
    }

    try (InputStream territoryTypeStream =
        getClass().getClassLoader().getResourceAsStream("ffxiv/resourcedata/TerritoryType.json")) {
      List<TerritoryType> loadedTerritoryTypeList =
          mapper.readValue(territoryTypeStream, new TypeReference<List<TerritoryType>>() {});
      territoryTypeList = Collections.unmodifiableList(loadedTerritoryTypeList);
    }

    try (InputStream weatherStream =
        getClass().getClassLoader().getResourceAsStream("ffxiv/resourcedata/Weather.json")) {
      List<Weather> loadedWeatherList =
          mapper.readValue(weatherStream, new TypeReference<List<Weather>>() {});
      weatherList = Collections.unmodifiableList(loadedWeatherList);
    }
  }

  @Override
  public List<WeatherConversationResult> getWeatherProbability(String userInputParam) {

    BotConversationConfig botConversationConfig =
        botConversationConfigRepository
            .findById(
                BotConversationConfigPrimaryKey.builder()
                    .topic("weather")
                    .conversationId(userInputParam)
                    .build())
            .orElse(null);
    if (botConversationConfig == null) {
      throw new RuntimeException("無此選項");
    }

    String targetPlaceNameEn = botConversationConfig.getDetail();
    String targetPlaceNameChs = getPlaceNameChs(targetPlaceNameEn);

    // Get seconds since Jan 1st 1970
    long earthEpochSeconds = Instant.now().getEpochSecond();
    // Get Eorzean hours/days since (1 Eorzean hour = 175 seconds)
    long eorzeanEpochHours = (long) FFXIVTimeUtil.getEorzeanEpochHours(earthEpochSeconds);
    // calculate when is this weather interval start, and convert to earth time
    long earthEpochSecondsWhenEorzeanWeatherIntervalStart =
        getEarthEpochTimeWhenEorzeaWeatherIntervalStart(eorzeanEpochHours);
    long eorzeanEpochHoursWhenEorzeanWeatherIntervalStart =
        (long) FFXIVTimeUtil.getEorzeanEpochHours(earthEpochSecondsWhenEorzeanWeatherIntervalStart);

    log.info(
        "earth epochSeconds when eorzean weather interval start:"
            + earthEpochSecondsWhenEorzeanWeatherIntervalStart);
    LocalDateTime localDateTimeWhenWeatherIntervalStart =
        FFXIVTimeUtil.getZoneEarthTime(
            earthEpochSecondsWhenEorzeanWeatherIntervalStart, ZoneId.of("Asia/Taipei"));

    String formattedDateTime = localDateTimeWhenWeatherIntervalStart.format(YYYY_MM_DD);
    log.info("interval start earth time: " + formattedDateTime); // Output: 2021-06-29 12:13:51
    log.info(
        "interval start ET: "
            + FFXIVTimeUtil.convertEarthTimeToEorzeanTime(
                earthEpochSecondsWhenEorzeanWeatherIntervalStart)); // EorzeanTime

    List<WeatherConversationResult> weatherConversationResultList = new ArrayList<>();
    long targetEorzeanEpochHours = eorzeanEpochHoursWhenEorzeanWeatherIntervalStart;
    for (int i = 1; i <= DEFAULT_WEATHER_COUNT; i++) {
      long targetEarthEpochSeconds =
          getEarthEpochTimeWhenEorzeaWeatherIntervalStart(targetEorzeanEpochHours);

      int forecastTarget = getForecastTarget(targetEorzeanEpochHours);
      Weather targetWeather = getTargetWeather(targetPlaceNameEn, forecastTarget);

      List<WeatherConversationResultDetail> weatherConversationResultDetailList = null;
      if (weatherConversationResultList.isEmpty()) {
        WeatherConversationResult weatherConversationResult = new WeatherConversationResult();
        weatherConversationResult.setPlaceName(targetPlaceNameChs);
        weatherConversationResultDetailList = new ArrayList<>();
        weatherConversationResult.setDetails(weatherConversationResultDetailList);
        weatherConversationResultList.add(weatherConversationResult);
      } else {
        weatherConversationResultDetailList =
            Objects.requireNonNull(
                    weatherConversationResultList.stream()
                        .filter(
                            result -> StringUtils.equals(targetPlaceNameChs, result.getPlaceName()))
                        .findFirst()
                        .orElse(null))
                .getDetails();
      }
      WeatherConversationResultDetail weatherConversationResultDetail =
          new WeatherConversationResultDetail();
      weatherConversationResultDetail.setWeatherName(targetWeather.getNameChs());
      weatherConversationResultDetail.setEarthTime(
          FFXIVTimeUtil.getZoneEarthTime(targetEarthEpochSeconds, ZoneId.of("Asia/Taipei")));
      weatherConversationResultDetail.setEorzeanTime(
          FFXIVTimeUtil.convertEarthTimeToEorzeanTime(targetEarthEpochSeconds));

      weatherConversationResultDetailList.add(weatherConversationResultDetail);
      targetEorzeanEpochHours = targetEorzeanEpochHours + 8;
    }

    return weatherConversationResultList;
  }

  @Override
  public String getPlaceNameChs(String targetPlaceNameEn) {

    TerritoryType targetTerritoryType = getTerritoryType(targetPlaceNameEn.trim());

    return targetTerritoryType != null ? targetTerritoryType.getPlaceName().getNameChs() : "";
  }

  private Weather getTargetWeather(String targetPlaceNameEn, int forecastTarget) {
    TerritoryType targetTerritoryType = getTerritoryType(targetPlaceNameEn);

    WeatherRateIndices targetWeatherRateIndices =
        weatherRateIndicesList.stream()
            .filter(
                weatherRateIndices ->
                    weatherRateIndices.getId() == targetTerritoryType.getWeatherRateId())
            .findFirst()
            .orElse(null);

    Rates targetRate =
        targetWeatherRateIndices.getRates().stream()
            .filter(rate -> forecastTarget < rate.getRate())
            .findFirst()
            .orElse(null);

    return weatherList.stream()
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
    // todo 解決計算結果不一致的問題
    long eorzeanEpochDays = eorzeanEpochHours / 24;
    long timeChunk = (eorzeanEpochHours % 24) - (eorzeanEpochHours % 8);
    timeChunk = (timeChunk + 8) % 24;

    int seed = (int) (eorzeanEpochDays * 100 + timeChunk);

    // Do a little hashing
    int step1 = (seed << 11) ^ seed;
    int step2 = (step1 >>> 8) ^ step1;

    // Return a number between 0-99 inclusive
    int forecastTarget = (int) (step2 % 100);
    return forecastTarget;
  }
}
