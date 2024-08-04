package com.ffxiv.linerobot.service.impl;

import com.ffxiv.crawler.service.CrawlerService;
import com.ffxiv.linerobot.dto.weather.WeatherConversationResult;
import com.ffxiv.linerobot.entity.BotConversationConfig;
import com.ffxiv.linerobot.entity.LineUserProfile;
import com.ffxiv.linerobot.repository.BotConversationConfigRepository;
import com.ffxiv.linerobot.repository.LineUserProfileRepository;
import com.ffxiv.linerobot.service.ConversationService;
import com.ffxiv.linerobot.service.WeatherService;
import com.ffxiv.linerobot.util.FFXIVTimeUtil;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class ConversationServiceImpl implements ConversationService {

  private static final DateTimeFormatter HH_MM_SS = DateTimeFormatter.ofPattern("HH:mm:ss");
  @Resource private RedisTemplate<String, Object> redisTemplate;
  @Autowired private WeatherService weatherService;
  @Autowired private BotConversationConfigRepository botConversationConfigRepository;
  @Autowired private CrawlerService crawlerService;
    @Autowired
    private LineUserProfileRepository lineUserProfileRepository;

  @Override
  public String getReply(LineUserProfile lineUserProfile, String receiveText) {
    String userId = lineUserProfile.getUserId();
    String reply = "";
    List<BotConversationConfig> nextLevelConversation = null;
    String userInputParam = receiveText;
    log.info("userInputParam:" + userInputParam);
    if (StringUtils.equals(userInputParam, "0")) {
      removeConversationSession(userId);
      return "此次對話已結束，如還有任何需要，請重新呼叫塔塔露~";
    }

    try {
      if (!isConversationSessionExists(userId)) {

        nextLevelConversation = getConversationByParentId("");
        String successConversationTitle = getSuccessConversationTitle(lineUserProfile);
        setConversationSession(userId, "", "");
        log.info("設定完conversationSession");
        reply =
            composeWeatherConversationReplyOptions(successConversationTitle, nextLevelConversation, userId);
      } else {
        Map<String, String> conversationSession = getConversationSession(userId);
        String topic = conversationSession.get("topic");
        String parentId = conversationSession.get("parent_id");
        // 輸入x則回到第一層選單
        if (StringUtils.equals(StringUtils.lowerCase(userInputParam), "x")) {
          removeConversationSession(userId);
          getReply(lineUserProfile, receiveText);
        }
        validateUserInputAgainstConfig(topic, parentId, userInputParam);
        switch (topic) {
          case "weather":
            nextLevelConversation = getNextLevelConversation(topic, userInputParam);
            if (nextLevelConversation.isEmpty()) {
              // @weatherParameter 為 bot_conversation_config.conversation_id
              // 沒有下一階對話時代表要回的不是對話選項而是處理的結果
              // todo 回覆結果的地球時間要格式化，只秀時分秒就好
              // todo 結束對話時應將session結束，並在對話內容內提示?
              List<WeatherConversationResult> weatherConversationResult =
                  weatherService.getWeatherProbability(userInputParam);

              reply = composeWeatherConversationReplyResult(weatherConversationResult);
              removeConversationSession(userId);

              break;
            } else {
              String successConversationTitle = getSuccessConversationTitle(lineUserProfile);
              reply =
                  composeWeatherConversationReplyOptions(
                      successConversationTitle, nextLevelConversation, userId);
              setConversationSession(userId, "weather", userInputParam);
              break;
            }
            //                case "time":
          default:
            // 看完index後，user發送的訊息會在這邊，此時session中的topic因為還沒進入任一個主題，所以是""
            String successConversationTitle = getSuccessConversationTitle(lineUserProfile);
            if (userInputParam.equals("a1")) {
              nextLevelConversation = getNextLevelConversation("weather", userInputParam);
              reply =
                  composeWeatherConversationReplyOptions(
                      successConversationTitle, nextLevelConversation, userId);
              setConversationSession(userId, "weather", userInputParam);
            } else if (userInputParam.equals("a2")) {
              // 因為沒有下一層選項所以回覆且刪除session
              reply =
                  FFXIVTimeUtil.convertEarthTimeToEorzeanTime(Instant.now().getEpochSecond())
                      .toString();
              removeConversationSession(userId);
            } else if (userInputParam.equals("a3")) {
              // 因為沒有下一層選項所以回覆且刪除session
              // fixme line回覆有字數限制，先只回部分字來代表功能正常
              String houseList = crawlerService.getHouseList();
              reply = houseList.length() > 100 ? houseList.substring(0, 100) : houseList;
              removeConversationSession(userId);
            } else if (userInputParam.equals("a4")) {

              List<LineUserProfile> all = lineUserProfileRepository.findAll();
              StringBuilder result = new StringBuilder();

// 添加標題
              result.append(String.format("%-20s %s%n", "Line暱稱", "遊戲ID"));
              result.append("----------------------------------------\n");

              for (LineUserProfile line : all) {
                String formattedLine = String.format("%-20s %s%n",
                        truncateOrPad(line.getDisplayName(), 20),
                        line.getFfxivId() != null ? line.getFfxivId() : "");
                result.append(formattedLine);
              }

              reply =  result.toString();
              removeConversationSession(userId);
            }
            break;
        }
      }
    } catch (Exception e) {
      log.error(e.getMessage());
      reply = "很抱歉，我不知道你說的是甚麼，請再次選擇，或按X回到上一層\n";
    }
    if (!isConversationSessionExists(userId)) {
      reply = reply + "\n此次對話已結束，如還有任何需要，請重新呼叫塔塔露~";
    }

    return reply;
  }

  private static String truncateOrPad(String input, int width) {
    if (input == null) {
      input = "";
    }
    if (input.length() > width) {
      return input.substring(0, width);
    } else {
      return String.format("%-" + width + "s", input);
    }
  }

  private void removeConversationSession(String userId) {
    String key = "user:" + userId + ":conversation";
    redisTemplate.delete(key);
  }

  private void validateUserInputAgainstConfig(
      String topic, String parentId, String userInputParam) {

    // todo 拋出自訂例外?  上層例外處理?
    List<BotConversationConfig> conversationList = getConversationByParentId(parentId);
    BotConversationConfig botConversationConfig =
        conversationList.stream()
            .filter(
                conversation ->
                    StringUtils.equals(conversation.getConversationId(), userInputParam))
            .findFirst()
            .orElseThrow();
  }

  private String composeWeatherConversationReplyResult(
      List<WeatherConversationResult> weatherConversationResult) {

    StringBuilder sb = new StringBuilder();
    weatherConversationResult.forEach(
        result -> {
          sb.append("區域:" + result.getPlaceName() + "\n");
          sb.append("地球時間,艾歐澤亞時間,天氣\n");
          result
              .getDetails()
              .forEach(
                  detail -> {
                    LocalDateTime earthTime = detail.getEarthTime();
                    LocalTime eorzeanTime = detail.getEorzeanTime();
                    String weatherName = detail.getWeatherName();

                    sb.append(earthTime.format(HH_MM_SS))
                        .append(",")
                        .append("ET:")
                        .append(eorzeanTime)
                        .append(",")
                        .append(weatherName);
                    sb.append("\n");
                  });
        });

    return sb.toString();
  }

  private String parseReceiveTextToConversationParam(String receiveText) {

    return StringUtils.substringAfter(receiveText, "塔塔露").trim().toLowerCase();
  }

  private List<BotConversationConfig> getConversationByParentId(String parentId) {

    return botConversationConfigRepository.findByParentId(parentId);
  }

  private String getSuccessConversationTitle(LineUserProfile lineUserProfile) {
    String displayName = lineUserProfile.getDisplayName();

    return "嗨~" + displayName + "，以下是可選擇的選項\n";
  }

  private String composeWeatherConversationReplyOptions(
      String conversationTitle, List<BotConversationConfig> list, String userId) {
    if (list.isEmpty()) {
      removeConversationSession(userId);
      return "很抱歉，我不知道你說的是甚麼，請重新開始對話\n";
    } else {

      list.sort(
          (o1, o2) -> {
            // 提取開頭的 字母 後的數字部分
            String conversationId1 = o1.getConversationId().substring(1);
            String conversationId2 = o2.getConversationId().substring(1);

            Integer num1 = Integer.parseInt(conversationId1);
            Integer num2 = Integer.parseInt(conversationId2);
            return num1.compareTo(num2);
          });

      String listString =
          list.stream()
              .map(config -> String.join(",", config.getConversationId(), config.getDetail()))
              .collect(Collectors.joining("\n"));

      return conversationTitle + "\n" + listString;
    }
  }

  private List<BotConversationConfig> getNextLevelConversation(String topic, String parentId) {

    List<BotConversationConfig> byTopicAndParentId =
        botConversationConfigRepository.findByTopicAndParentId(topic, parentId);

    byTopicAndParentId.forEach(
        config -> {
          if (StringUtils.startsWith(config.getConversationId(), "b")
              && StringUtils.equals(topic, "weather")) {
            String placeNameChs = weatherService.getPlaceNameChs(config.getDetail());
            config.setDetail(placeNameChs);
          }
        });

    return byTopicAndParentId;
  }

  private Map<String, String> getConversationSession(String userId) {
    String key = "user:" + userId + ":conversation";
    Map<String, String> conversationSession =
        (Map<String, String>) redisTemplate.opsForValue().get(key);

    return conversationSession;
  }

  private void setConversationSession(String userId, String topic, String currentParentId) {
    String key = "user:" + userId + ":conversation";
    Map<String, String> conversationSession = new HashMap<>();
    conversationSession.put("topic", topic);
    conversationSession.put("parent_id", currentParentId);

    redisTemplate.opsForValue().set(key, conversationSession, 3, TimeUnit.MINUTES);
  }

  //    確認當前是否有正在進行的會話
  @Override
  public boolean isConversationSessionExists(String userId) {
    String key = "user:" + userId + ":conversation";
    Boolean exists = redisTemplate.hasKey(key);
    return Boolean.TRUE.equals(exists);
  }
}
