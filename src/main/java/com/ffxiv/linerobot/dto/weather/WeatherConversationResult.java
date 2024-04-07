package com.ffxiv.linerobot.dto.weather;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeatherConversationResult {

  private String placeName;

  private List<WeatherConversationResultDetail> details;
}
