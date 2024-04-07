package com.ffxiv.linerobot.service;

import com.ffxiv.crawler.service.CrawlerService;
import java.io.IOException;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LineRobotPushService {
  private OkHttpClient client = new OkHttpClient();

  @Value("${line.user.channel.token}")
  private String LINE_MESSAGING_TOKEN;

  @Value("${line.user.notify.token}")
  private String LINE_NOTIFY_TOKEN;

  @Value("${lineuser.myid}")
  private String LINE_MY_ID;

  @Autowired private CrawlerService crawlerService;

  public void text(String text) {
    JSONObject body = new JSONObject();
    JSONArray messages = new JSONArray();
    JSONObject message = new JSONObject();
    message.put("type", "text");
    message.put("text", text);
    messages.put(message);
    body.put("to", LINE_MY_ID);
    body.put("messages", messages);
    pushLinePlatform(body);
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
    pushLinePlatform(body);
  }

  public void pushLinePlatform(JSONObject json) {
    Request request =
        new Request.Builder()
            .url("https://api.line.me/v2/bot/message/push")
            .header("Authorization", "Bearer {" + LINE_MESSAGING_TOKEN + "}")
            .post(RequestBody.create(MediaType.parse("application/json"), json.toString()))
            .build();
    client
        .newCall(request)
        .enqueue(
            new Callback() {

              @Override
              public void onResponse(Call call, Response response) throws IOException {
                System.out.println(response.body());
              }

              @Override
              public void onFailure(Call call, IOException e) {
                System.err.println(e);
              }
            });
  }

  public void pushToLineNotify(String string) {

    // FormBody自帶ContentType=application/x-www-form-urlencoded
    FormBody formBody = new FormBody.Builder().add("message", string).build();

    Request request =
        new Request.Builder()
            .url("https://notify-api.line.me/api/notify")
            .header("Authorization", "Bearer " + LINE_NOTIFY_TOKEN)
            .post(formBody)
            .build();

    client
        .newCall(request)
        .enqueue(
            new Callback() {

              @Override
              public void onResponse(Call call, Response response) throws IOException {
                System.out.println(response.body().string());
              }

              @Override
              public void onFailure(Call call, IOException e) {
                System.err.println(e);
              }
            });
  }
}
