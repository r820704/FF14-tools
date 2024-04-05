package com.ffxiv.linerobot.dto.weather;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class WeatherConversationResult {

    private String placeName;

    private List<WeatherConversationResultDetail> details;

}
