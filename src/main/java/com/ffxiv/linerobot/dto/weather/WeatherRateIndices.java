package com.ffxiv.linerobot.dto.weather;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@JsonPropertyOrder({"id", "rates"})
@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeatherRateIndices {

    private int Id;

    private List<Rates> rates;
}
