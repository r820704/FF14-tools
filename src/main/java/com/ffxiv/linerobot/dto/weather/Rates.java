package com.ffxiv.linerobot.dto.weather;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonPropertyOrder({"weather", "rate"})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Rates {

  private int weather;

  private int rate;
}
