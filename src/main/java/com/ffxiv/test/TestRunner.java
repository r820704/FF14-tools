package com.ffxiv.test;

import com.ffxiv.linerobot.entity.LineUserProfile;
import com.ffxiv.linerobot.service.impl.ConversationServiceImpl;
import java.util.Scanner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class TestRunner implements CommandLineRunner {

  @Autowired private ConversationServiceImpl conversationService;

  @Override
  public void run(String... args) throws Exception {
    try (Scanner scanner = new Scanner(System.in)) {
      System.out.print("請輸入userid: ");
      String userId = scanner.nextLine();
      LineUserProfile userProfile = new LineUserProfile();
      userProfile.setUserId(userId);
      userProfile.setDisplayName("測試DisplayName");

      System.out.println("請輸入測試指令:");
      while (true) {
        String input = scanner.nextLine();
        if ("退出".equals(input)) {
          break;
        }
        // 模拟LineUserProfile输入

        String reply = conversationService.getReply(userProfile, input);
        System.out.println(reply);
      }
    }
  }
}
