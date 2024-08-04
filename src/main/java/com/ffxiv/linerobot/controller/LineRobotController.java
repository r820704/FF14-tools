package com.ffxiv.linerobot.controller;

import com.ffxiv.linerobot.entity.LineUserProfile;
import com.ffxiv.linerobot.repository.LineUserProfileRepository;
import com.ffxiv.linerobot.service.LineRobotPushService;
import com.ffxiv.linerobot.service.LineRobotService;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequestMapping("/robot")
@RestController
@Slf4j
public class LineRobotController {

  @Value("${line.user.secret}")
  private String LINE_SECRET;

  @Autowired private LineRobotService lineRobotService;
  @Autowired private LineRobotPushService lineRobotPushService;
  @Autowired private LineUserProfileRepository lineUserRepository;
  @Autowired private RedisTemplate<String, String> redisTemplate;

  @GetMapping("/test")
  public ResponseEntity test() {

//    lineUserRepository.saveAndFlush(
//        new LineUserProfile(String.valueOf(Math.random()), "test", "test", "test", "test", "test"));
//    redisTemplate.opsForValue().set(String.valueOf(Math.random()), "test");

    return new ResponseEntity("Hello J A V A!!", HttpStatus.OK);
  }

  @PostMapping("/messaging")
  public ResponseEntity messagingAPI(
      @RequestHeader("X-Line-Signature") String X_Line_Signature, @RequestBody String requestBody)
      throws IOException, InterruptedException, JSONException {
    if (checkFromLine(requestBody, X_Line_Signature)) {
      log.info("驗證通過");
      JSONObject object = new JSONObject(requestBody);
      for (int i = 0; i < object.getJSONArray("events").length(); i++) {
        if (object.getJSONArray("events").getJSONObject(i).getString("type").equals("message")) {
          lineRobotService.doAction(object.getJSONArray("events").getJSONObject(i));
        }
      }
      return new ResponseEntity<String>("OK", HttpStatus.OK);
    }
    log.info("驗證不通過");
    return new ResponseEntity<String>("Not line platform", HttpStatus.BAD_GATEWAY);
  }

  @GetMapping("/pushtolinenotify")
  public ResponseEntity pushtolinenotify() {
    lineRobotPushService.pushToLineNotify("Push to Line Notify!!!");

    return new ResponseEntity<String>("OK", HttpStatus.OK);
  }

  @GetMapping("/pushtolineplatform")
  public ResponseEntity pushtolineplatform() {
    lineRobotPushService.text("Push to Line Platform!!!");

    return new ResponseEntity<String>("OK", HttpStatus.OK);
  }

  public boolean checkFromLine(String requestBody, String X_Line_Signature) {
    SecretKeySpec key = new SecretKeySpec(LINE_SECRET.getBytes(), "HmacSHA256");
    Mac mac;
    try {
      mac = Mac.getInstance("HmacSHA256");
      mac.init(key);
      byte[] source = requestBody.getBytes(StandardCharsets.UTF_8);
      String signature = Base64.encodeBase64String(mac.doFinal(source));
      if (signature.equals(X_Line_Signature)) {
        return true;
      }
    } catch (NoSuchAlgorithmException | InvalidKeyException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }

    return false;
  }
}
