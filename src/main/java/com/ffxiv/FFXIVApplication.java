package com.ffxiv;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FFXIVApplication {
  private static final Logger log = LogManager.getLogger(FFXIVApplication.class);

  public static void main(String[] args) {
    log.info("FF14工具 開始執行！");
    SpringApplication.run(FFXIVApplication.class, args);
  }
}
