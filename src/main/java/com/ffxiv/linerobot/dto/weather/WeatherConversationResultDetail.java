package com.ffxiv.linerobot.dto.weather;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeatherConversationResultDetail {

    private LocalDateTime earthTime;

    private LocalTime eorzeanTime;

    private String weatherName;

}
