package com.ffxiv.linerobot.dto.weather;

import java.time.LocalDateTime;
import java.time.LocalTime;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeatherConversationResultDetail {

  private LocalDateTime earthTime;

  private LocalTime eorzeanTime;

  private String weatherName;
}
