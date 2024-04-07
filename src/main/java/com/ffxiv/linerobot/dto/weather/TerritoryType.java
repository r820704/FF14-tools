package com.ffxiv.linerobot.dto.weather;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TerritoryType {

  @JsonProperty("PlaceName")
  private PlaceName placeName;

  @JsonProperty("WeatherRate")
  private int weatherRateId;
}
