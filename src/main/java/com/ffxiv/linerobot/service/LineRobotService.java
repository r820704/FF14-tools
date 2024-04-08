package com.ffxiv.linerobot.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ffxiv.crawler.service.CrawlerService;
import com.ffxiv.linerobot.entity.LineUserProfile;
import com.ffxiv.linerobot.entity.pk.LineUserProfilePrimaryKey;
import com.ffxiv.linerobot.repository.LineUserProfileRepository;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import javax.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class LineRobotService {
  private OkHttpClient client = new OkHttpClient();

  @Value("${line.user.channel.token}")
  private String LINE_MESSAGING_TOKEN;

  @Value("${line.user.notify.token}")
  private String LINE_NOTIFY_TOKEN;

  @Autowired private CrawlerService crawlerService;
  @Autowired private LineUserProfileRepository lineUserProfileRepository;
  @Resource private RedisTemplate<String, Object> redisTemplate;
  @Autowired private ConversationService conversationService;

  public void doAction(JSONObject event) throws InterruptedException {

    switch (event.getJSONObject("message").getString("type")) {
      case "text":
        String groupId = event.getJSONObject("source").getString("groupId");
        String userId = event.getJSONObject("source").getString("userId");
        // 使用supplyAsync來異步執行getLineUserProfileAsync，同時指定Executor來使用新的執行緒
        getLineUserProfileAsync(groupId, userId)
            .thenAccept(
                lineUserProfile -> {
                  // 此處在getLineUserProfileAsync的異步操作完成後執行
                  try {
                    handleTextMessage(event, lineUserProfile); // 這裡假設handleTextMessage已經適配異步執行
                  } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                  }
                });
        break;
        //  case "sticker":
        //      sticker(event.getString("replyToken"),
        // event.getJSONObject("message").getString("packageId"),
        //              event.getJSONObject("message").getString("stickerId"));
        //      break;
    }
  }

  private void handleTextMessage(JSONObject event, LineUserProfile lineUserProfile)
      throws InterruptedException {
    String receiveText = event.getJSONObject("message").getString("text");
    String houseList = null;
    log.info("line收到的訊息為: " + receiveText);
    // todo 這邊當對話進行中時，應要先確認session是否存在，再呼叫conversationService 例如輸入: b1
    if (receiveText.startsWith("塔塔露")
        || conversationService.isConversationSessionExists(lineUserProfile.getUserId())) {
      log.info("準備呼叫conversationService");
      String reply = conversationService.getReply(lineUserProfile, receiveText);
      text(event.getString("replyToken"), reply);
    } else if (receiveText.startsWith("!房屋")) {
      houseList = crawlerService.getHouseList();
      text(event.getString("replyToken"), houseList.substring(0, 100));
    } else if (receiveText.startsWith("!機器人")) {
      text(
          event.getString("replyToken"),
          lineUserProfile.getDisplayName() + " 庫啵! 我現在是沒有功能的廢物機器人(′゜ω。‵)");
    }
  }

  private void text(String replyToken, String text) {
    JSONObject body = new JSONObject();
    JSONArray messages = new JSONArray();
    JSONObject message = new JSONObject();
    message.put("type", "text");
    message.put("text", text);

    // 創建 sender 物件並加入所需的屬性
    JSONObject sender = new JSONObject();
    sender.put("name", "塔塔露");
    sender.put("iconUrl", "https://i.imgur.com/3D1i1Ju.png");

    // 將 sender 物件加入到 message 物件中
    message.put("sender", sender);

    messages.put(message);
    body.put("replyToken", replyToken);
    body.put("messages", messages);
    sendLinePlatform(body);
  }

  private void sticker(String replyToken, String packageId, String stickerId) {
    JSONObject body = new JSONObject();
    JSONArray messages = new JSONArray();
    JSONObject message = new JSONObject();
    message.put("type", "sticker");
    message.put("packageId", packageId);
    message.put("stickerId", stickerId);
    messages.put(message);
    body.put("replyToken", replyToken);
    body.put("messages", messages);
    sendLinePlatform(body);
  }

  public void sendLinePlatform(JSONObject json) {
    Request request =
        new Request.Builder()
            .url("https://api.line.me/v2/bot/message/reply")
            .header("Authorization", "Bearer {" + LINE_MESSAGING_TOKEN + "}")
            .post(
                RequestBody.create(
                    MediaType.parse("application/json; charset=utf-8"), json.toString()))
            .build();
    client
        .newCall(request)
        .enqueue(
            new Callback() {

              @Override
              public void onResponse(Call call, Response response) throws IOException {
                try (ResponseBody responseBody = response.body()) {
                  if (!response.isSuccessful())
                    throw new IOException("Unexpected code " + response);

                  String result = responseBody.string();
                  log.info("Line reply api result:" + result);
                }
              }

              @Override
              public void onFailure(Call call, IOException e) {
                System.err.println(e);
              }
            });
  }

  public CompletableFuture<LineUserProfile> getLineUserProfileAsync(String groupId, String userId) {
    CompletableFuture<LineUserProfile> future = new CompletableFuture<>();
    String url = "https://api.line.me/v2/bot/group/" + groupId + "/member/" + userId;

    Request request =
        new Request.Builder()
            .url(url)
            .header("Authorization", "Bearer " + LINE_MESSAGING_TOKEN)
            .build();

    client
        .newCall(request)
        .enqueue(
            new Callback() {
              @Override
              public void onResponse(Call call, Response response) {
                try (ResponseBody responseBody = response.body()) {
                  if (!response.isSuccessful()) {
                    future.completeExceptionally(new IOException("Unexpected code " + response));
                    return;
                  }

                  String result = responseBody.string();
                  LineUserProfile userProfile = parseUserProfile(result);
                  if (!lineUserProfileRepository.existsById(
                      new LineUserProfilePrimaryKey(
                          userProfile.getChannel(), userProfile.getUserId()))) {
                    lineUserProfileRepository.saveAndFlush(userProfile);
                  }
                  future.complete(userProfile); // 完成 CompletableFuture
                } catch (IOException e) {
                  future.completeExceptionally(e);
                } finally {
                  response.close(); // 确保响应体被关闭
                }
              }

              @Override
              public void onFailure(Call call, IOException e) {
                future.completeExceptionally(e);
              }
            });

    return future;
  }

  private LineUserProfile parseUserProfile(String result) throws JsonProcessingException {

    ObjectMapper objectMapper = new ObjectMapper();
    LineUserProfile lineUserProfile = objectMapper.readValue(result, LineUserProfile.class);
    lineUserProfile.setChannel("Messaging API");
    return lineUserProfile;
  }
}
