package com.ffxiv.linerobot.dto.weather;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PlaceName {

  @JsonProperty("ID")
  private String id;

  @JsonProperty("Name_chs")
  private String nameChs;

  @JsonProperty("Name_en")
  private String nameEn;

  @JsonProperty("Name_ja")
  private String nameJa;
}
