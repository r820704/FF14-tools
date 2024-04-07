package com.ffxiv.linerobot.service;

import com.ffxiv.linerobot.dto.weather.WeatherConversationResult;

import java.util.List;

public interface WeatherService {
    List<WeatherConversationResult> getWeatherProbability(String userInputParam);

    String getPlaceNameChs(String targetPlaceNameEn);
}
