package com.ff14;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

@SpringBootApplication
public class FF14Application {
	private static final Logger log = LogManager.getLogger(FF14Application.class);

	public static void main(String[] args) {
		log.info("FF14工具 開始執行！");
		SpringApplication.run(FF14Application.class, args);
	}

	

}
