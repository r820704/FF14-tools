package com.ff14.linerobot.service;

import java.io.IOException;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openqa.selenium.WebDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ff14.crawler.service.CrawlerService;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.Buffer;
import okio.BufferedSink;

@Component
@Slf4j
public class LineRobotService {
	private OkHttpClient client = new OkHttpClient();
	@Value("${line.user.channel.token}")
	private String LINE_MESSAGING_TOKEN;
	@Value("${line.user.notify.token}")
	private String LINE_NOTIFY_TOKEN;

	@Autowired
	private CrawlerService crawlerService;
	
	public void doAction(JSONObject event) throws InterruptedException {
		switch (event.getJSONObject("message").getString("type")) {
		case "text":
			String receiveText = event.getJSONObject("message").getString("text");
			String houseList = null ;
System.out.println("line收到的訊息為: " + receiveText);	
			if(receiveText.startsWith("!房屋")) {
				houseList = crawlerService.getHouseList();
				text(event.getString("replyToken"), houseList.substring(0,100));
			}else if(receiveText.startsWith("!機器人")) {
				text(event.getString("replyToken"), "庫啵! 我現在是沒有功能的廢物機器人(′゜ω。‵)");
			};
			break;
//		case "sticker":
//			sticker(event.getString("replyToken"), event.getJSONObject("message").getString("packageId"),
//					event.getJSONObject("message").getString("stickerId"));
//			break;
		}
	}

	private void text(String replyToken, String text) {
		JSONObject body = new JSONObject();
		JSONArray messages = new JSONArray();
		JSONObject message = new JSONObject();
		message.put("type", "text");
		message.put("text", text);
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
		Request request = new Request.Builder().url("https://api.line.me/v2/bot/message/reply")
				.header("Authorization", "Bearer {" + LINE_MESSAGING_TOKEN + "}")
				.post(RequestBody.create(MediaType.parse("application/json; charset=utf-8"), json.toString())).build();
		client.newCall(request).enqueue(new Callback() {

			@Override
			public void onResponse(Call call, Response response) throws IOException {
				String result = response.body().string();
				log.info("Line reply api result:" + result);
			}

			@Override
			public void onFailure(Call call, IOException e) {
				System.err.println(e);
			}
		});
	}
	
	
}
