package com.ffxiv.test;

import com.ffxiv.linerobot.entity.LineUserProfile;
import com.ffxiv.linerobot.service.impl.ConversationServiceImpl;
import java.util.Scanner;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
@Slf4j
public class TestRunner implements CommandLineRunner {

  @Autowired private ConversationServiceImpl conversationService;

  @Override
  public void run(String... args) {
    try (Scanner scanner = new Scanner(System.in)) {
      System.out.print("請輸入userid: ");
      String userId = scanner.nextLine();
      LineUserProfile userProfile = new LineUserProfile();
      userProfile.setUserId(userId);
      userProfile.setDisplayName("測試DisplayName");

      log.info("請輸入測試指令:");
      while (true) {
        String input = scanner.nextLine();
        if ("退出".equals(input)) {
          break;
        }
        // 模拟LineUserProfile输入

        String reply = conversationService.getReply(userProfile, input);
        log.info(reply);
      }
    }
  }
}
