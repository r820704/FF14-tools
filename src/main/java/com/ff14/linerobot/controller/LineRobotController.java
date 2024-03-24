package com.ff14.linerobot.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.ff14.linerobot.entity.LineUserProfile;
import com.ff14.linerobot.repository.LineUserProfileRepository;
import org.apache.tomcat.util.codec.binary.Base64;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ff14.linerobot.service.LineRobotPushService;
import com.ff14.linerobot.service.LineRobotService;

@RequestMapping("/robot")
@RestController
public class LineRobotController {

	@Value("${line.user.secret}")
	private String LINE_SECRET;
	
	@Autowired
	private LineRobotService lineRobotService;
	@Autowired
	private LineRobotPushService lineRobotPushService;
	@Autowired
	private LineUserProfileRepository lineUserRepository;


	@GetMapping("/test")
	public ResponseEntity test() {

		lineUserRepository.saveAndFlush(new LineUserProfile(String.valueOf(Math.random()),"test","test","test","test","test"));

		return new ResponseEntity("Hello J A V A!!", HttpStatus.OK);
	}


	@PostMapping("/messaging")
	public ResponseEntity messagingAPI(@RequestHeader("X-Line-Signature") String X_Line_Signature,
			@RequestBody String requestBody) throws UnsupportedEncodingException, IOException, InterruptedException, JSONException {
		if (checkFromLine(requestBody, X_Line_Signature)) {
			System.out.println("驗證通過");
			JSONObject object = new JSONObject(requestBody);
			for (int i = 0; i < object.getJSONArray("events").length(); i++) {
				if (object.getJSONArray("events").getJSONObject(i).getString("type").equals("message")) {
					lineRobotService.doAction(object.getJSONArray("events").getJSONObject(i));
				}
			}
			return new ResponseEntity<String>("OK", HttpStatus.OK);
		}
		System.out.println("驗證不通過");
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
			byte[] source = requestBody.getBytes("UTF-8");
			String signature = Base64.encodeBase64String(mac.doFinal(source));
			if (signature.equals(X_Line_Signature)) {
				return true;
			}
		} catch (NoSuchAlgorithmException | InvalidKeyException | UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return false;
	}
}
