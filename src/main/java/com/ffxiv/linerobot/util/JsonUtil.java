package com.ffxiv.linerobot.util;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public class JsonUtil {

  public static <T> List<T> readJsonFileToList(String filePath, Class<T> clazz, ObjectMapper mapper)
      throws IOException {
    //        ObjectMapper mapper = new ObjectMapper();

    // 讀取 JSON 檔案內容
    String jsonContent =
        new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);

    // 構造具有正確元素類型的 List 類型
    JavaType javaType = mapper.getTypeFactory().constructCollectionType(List.class, clazz);

    // 將 JSON 字串轉換為指定類型的 List
    return mapper.readValue(jsonContent, javaType);
  }
}
