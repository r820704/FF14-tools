package com.ffxiv.linerobot.resourcebuilder;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ffxiv.linerobot.dto.weather.Rates;
import com.ffxiv.linerobot.dto.weather.TerritoryType;
import com.ffxiv.linerobot.dto.weather.Weather;
import com.ffxiv.linerobot.dto.weather.WeatherRateIndices;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class WeatherResourceBuilder {

    private static List<WeatherRateIndices> parseWeatherRateIndicesJson(String jsonInput) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        JsonNode rootNode = mapper.readTree(jsonInput);
        // 访问 "skywatcher" 对象，然后从中获取 "weatherRateIndex"
        JsonNode weatherRateIndexNode = rootNode.path("skywatcher").path("weatherRateIndex");

        List<WeatherRateIndices> weatherRateIndicesList = new ArrayList<>();

        // 遍历 "weatherRateIndex" 的每个属性
        weatherRateIndexNode.fields().forEachRemaining(entry -> {

            int id = entry.getValue().path("id").asInt();
            List<Rates> rates = new ArrayList<>();

            // 遍历 "rates" 数组
            entry.getValue().path("rates").forEach(rateNode -> {
                int weatherId = rateNode.path("weather").asInt(); // 读取weather作为id
                int rateValue = rateNode.path("rate").asInt(); // 读取rate
                rates.add(new Rates(weatherId, rateValue)); // 创建WeatherRate对象并添加到列表中
            });

            weatherRateIndicesList.add(new WeatherRateIndices(id, rates)); // 创建WeatherRateIndices对象并添加到列表中
        });

        return weatherRateIndicesList;
    }

    private static <T> List<T> parseJson(String jsonInput, Class<T> clazz, ObjectMapper mapper) throws JsonProcessingException {
        JsonNode resultsNode = mapper.readTree(jsonInput).path("Results");
        List<T> resultList = new ArrayList<>();

        if (resultsNode.isArray()) {
            for (JsonNode resultNode : resultsNode) {
                T resultObject = mapper.treeToValue(resultNode, clazz);
                resultList.add(resultObject);
            }
        }

        return resultList;
    }

    public static void saveDataToFile(List<?> data, String filePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        String jsonResult = mapper.writeValueAsString(data);

        Path path = Paths.get(filePath);
        // 确保目标目录存在，如果不存在，则创建它
        Files.createDirectories(path.getParent());

        // 现在可以安全地写入文件了
        Files.write(path, jsonResult.getBytes());
    }

    public static void main(String[] args) {

        RestTemplate restTemplate = new RestTemplate();
        //TerritoryType.weatherRateId=WeatherRateIndices.id, WeatherRateIndices.rates.weather = Weather.id
        // prepare WeatherRateIndices.json

        String apiUrl = "https://www.garlandtools.org/db/doc/core/en/3/data.json";
        String jsonInput = restTemplate.getForObject(apiUrl, String.class);

        try {
            List<WeatherRateIndices> indices = parseWeatherRateIndicesJson(jsonInput);
            if (indices.isEmpty()) {
                System.out.println("No data parsed from JSON.");
            } else {
                System.out.println("Parsed data: " + indices.toString());
            }
            String filePath = "src/main/resources/ffxiv/resourcedata/WeatherRateIndices.json";
            saveDataToFile(indices, filePath);
            System.out.println("Data saved to " + filePath);
        } catch (IOException e) {
            e.printStackTrace();
        }

        // prepare TerritoryType.json

        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        List<TerritoryType> territoryTypeList = new ArrayList<>();
        int page = 1;
        int totalPages = 1; // 初始设为最大值，确保至少执行一次循环

        while (page <= totalPages) {
            apiUrl = "https://cafemaker.wakingsands.com/TerritoryType?columns=ID,WeatherRate,PlaceName&Page=" + page;
            jsonInput = restTemplate.getForObject(apiUrl, String.class);

            try {
                List<TerritoryType> pageTerritoryTypes = parseJson(jsonInput, TerritoryType.class, mapper);
                territoryTypeList.addAll(pageTerritoryTypes);

                // 更新总页数和当前页码
                JsonNode rootNode = mapper.readTree(jsonInput);
                JsonNode paginationNode = rootNode.path("Pagination");
                totalPages = paginationNode.path("PageTotal").asInt();
                page++; // 准备请求下一页

                System.out.println("Processed Page: " + (page - 1));
            } catch (IOException e) {
                e.printStackTrace();
                break; // 发生异常时退出循环
            }
        }

        if (!territoryTypeList.isEmpty()) {
            System.out.println("Parsed data: " + territoryTypeList);
            String filePath = "src/main/resources/ffxiv/resourcedata/TerritoryType.json";
            try {
                saveDataToFile(territoryTypeList, filePath);
                System.out.println("Data saved to " + filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No data parsed from JSON.");
        }

        // prepare

        List<Weather> weatherList = new ArrayList<>();
        page = 1;
        totalPages = 1; // 初始设为最大值，确保至少执行一次循环

        while (page <= totalPages) {
            apiUrl = "https://cafemaker.wakingsands.com/Weather?Columns=ID,Name_chs,Name_en,Name_ja&Page=" + page;
            jsonInput = restTemplate.getForObject(apiUrl, String.class);

            try {
                List<Weather> pageWeathers = parseJson(jsonInput, Weather.class, mapper);
                weatherList.addAll(pageWeathers);

                // 更新总页数和当前页码
                JsonNode rootNode = mapper.readTree(jsonInput);
                JsonNode paginationNode = rootNode.path("Pagination");
                totalPages = paginationNode.path("PageTotal").asInt();
                page++; // 准备请求下一页

                System.out.println("Processed Page: " + (page - 1));
            } catch (IOException e) {
                e.printStackTrace();
                break; // 发生异常时退出循环
            }
        }

        if (!weatherList.isEmpty()) {
            System.out.println("Parsed data: " + weatherList);
            String filePath = "src/main/resources/ffxiv/resourcedata/Weather.json";
            try {
                saveDataToFile(weatherList, filePath);
                System.out.println("Data saved to " + filePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            System.out.println("No data parsed from JSON.");
        }

    }

}
