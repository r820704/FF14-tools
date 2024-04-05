package com.ffxiv.linerobot.service.impl;

import com.ffxiv.linerobot.dto.weather.WeatherConversationResult;
import com.ffxiv.linerobot.entity.BotConversationConfig;
import com.ffxiv.linerobot.entity.LineUserProfile;
import com.ffxiv.linerobot.repository.BotConversationConfigRepository;
import com.ffxiv.linerobot.service.ConversationService;
import com.ffxiv.linerobot.service.WeatherService;
import com.ffxiv.linerobot.util.FFXIVTimeUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class ConversationServiceImpl implements ConversationService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;
    @Autowired
    private WeatherService weatherService;
    @Autowired
    private BotConversationConfigRepository botConversationConfigRepository;

    @Override
    public String getReply(LineUserProfile lineUserProfile, String receiveText) {
        String userId = lineUserProfile.getUserId();
        String reply = "";
        List<BotConversationConfig> nextLevelConversation = null;
        String userInputParam = parseReceiveTextToConversationParam(receiveText);

        if (!isConversationSessionExists(userId)) {
            nextLevelConversation = getConversationByParentId("");
            String successConversationTitle = getSuccessConversationTitle(lineUserProfile);
            setConversationSession(userId, "", "");
            reply = composeWeatherConversationReplyOptions(successConversationTitle, nextLevelConversation);
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
            nextLevelConversation = getNextLevelConversation(topic, userInputParam);
            switch (topic) {
                case "weather":
                    if (nextLevelConversation.isEmpty()) {
                        // @weatherParameter 為 bot_conversation_config.conversation_id
                        // 沒有下一階對話時代表要回的不是對話選項而是處理的結果

                        List<WeatherConversationResult> weatherConversationResult = weatherService.getWeatherProbability(userInputParam);

                        reply = composeWeatherConversationReplyResult(weatherConversationResult);
                        removeConversationSession(userId);

                        break;
                    } else {
                        String successConversationTitle = getSuccessConversationTitle(lineUserProfile);
                        reply = composeWeatherConversationReplyOptions(successConversationTitle, nextLevelConversation);
                        setConversationSession(userId, "weather", userInputParam);
                        break;
                    }
//                case "time":
                default:
                    // 看完index後，user發送的訊息會在這邊，此時session中的topic因為還沒進入任一個主題，所以是""
                    String successConversationTitle = getSuccessConversationTitle(lineUserProfile);
                    if (userInputParam.equals("a1")) {
                        reply = composeWeatherConversationReplyOptions(successConversationTitle, nextLevelConversation);
                        setConversationSession(userId, "weather", userInputParam);
                    } else if (userInputParam.equals("a2")) {
                        // 因為沒有下一層選項所以刪除session
                        reply = FFXIVTimeUtil.convertEarthTimeToEorzeanTime(Instant.now().getEpochSecond()).toString();
                        removeConversationSession(userId);
                    }
                    break;
            }
        }
        return reply;
    }

    private void removeConversationSession(String userId) {
        String key = "user:" + userId + ":conversation";
        redisTemplate.delete(key);

    }

    private void validateUserInputAgainstConfig(String topic, String parentId, String userInputParam) {

        //todo 拋出自訂例外?  上層例外處理?
        List<BotConversationConfig> conversationList = getConversationByParentId(parentId);
        BotConversationConfig botConversationConfig = conversationList
                .stream()
                .filter(conversation -> StringUtils.equals(conversation.getDetail(), userInputParam))
                .findFirst()
                .orElseThrow();

    }


    private String composeWeatherConversationReplyResult(List<WeatherConversationResult> weatherConversationResult) {
        StringBuilder sb = new StringBuilder();
        weatherConversationResult.forEach(result -> {
            sb.append("區域:" + result.getPlaceName() + "\n");
            sb.append("地球時間,艾歐澤亞時間,天氣");
            result.getDetails().forEach(detail -> {
                LocalDateTime earthTime = detail.getEarthTime();
                LocalTime eorzeanTime = detail.getEorzeanTime();
                String weatherName = detail.getWeatherName();

                sb.append(earthTime + "," + eorzeanTime + "," + weatherName);
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


    private String composeWeatherConversationReplyOptions(String conversationTitle, List<BotConversationConfig> list) {
        if (list.isEmpty()) {
            return "很抱歉，我不知道你說的是甚麼，請再次選擇，或按X回到上一層\n";
        } else {
            String listString = list.stream()
                    .map(config -> String.join(", ", config.getConversationId(), config.getDetail()))
                    .collect(Collectors.joining("\n"));

            return conversationTitle + "\n" + listString;
        }
    }


    private List<BotConversationConfig> getNextLevelConversation(String topic, String parentId) {

        return botConversationConfigRepository.findByTopicAndParentId(topic, parentId);
    }

    private Map<String, String> getConversationSession(String userId) {
        String key = "user:" + userId + ":conversation";
        Map<String, String> conversationSession = (Map<String, String>) redisTemplate.opsForValue().get(key);

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
