package com.ff14.crawler;

import javax.annotation.PreDestroy;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"com.ff14"})
public class CrawlerConfig {
	
	@Autowired
	private WebDriver driver;
	
	@Bean
	public WebDriver driver() {
		//若瀏覽器安裝位置為預設則webDriver會自動搜尋path設定的位置，也可以使用System.setProperty 來指定路徑
		System.setProperty("webdriver.chrome.driver", "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chromedriver.exe");
		//Selenium對不同瀏覽器提供了不同的webDriver
		return new ChromeDriver();
	}
	
	@PreDestroy
	private void destory() {
		System.out.println("關閉WebDriver");
		driver.close();
	}
	
}
