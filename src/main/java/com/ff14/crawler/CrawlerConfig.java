package com.ff14.crawler;

import javax.annotation.PreDestroy;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;

@Configuration
@ComponentScan(basePackages = {"com.ff14"})
public class CrawlerConfig {
	
	@Autowired
	@Lazy
	private WebDriver driver;
	
	@Value("${webdriver.chrome.driver}")
	private String WEBDRIVER_PATH ;
	
	@Bean
	public WebDriver driver(@Value("${webdriver.chrome.driver}") String WEBDRIVER_PATH) {

System.out.println("WEBDRIVER_PATH" + WEBDRIVER_PATH);
		//若瀏覽器安裝位置為預設則webDriver會自動搜尋path設定的位置，也可以使用System.setProperty 來指定路徑
		System.setProperty("webdriver.chrome.driver", WEBDRIVER_PATH);
		//Selenium對不同瀏覽器提供了不同的webDriver
		
		// 設定chromeDriver不要開啟Gui，使用無頭模式，要加上此段才能在linux環境順利產生ChromeDriver
        ChromeOptions options = new ChromeOptions();
//        options.addArguments("--headless","--disable-gpu");  // windows環境目前能正常啟動的參數 
        options.addArguments("--headless","--no-sandbox","--disable-dev-shm-usage"); // linux環境目前能正常啟動的參數 
        
		return new ChromeDriver(options);
	}
	
	@PreDestroy
	private void destory() {
		System.out.println("關閉WebDriver");
		driver.close();
	}
	
}
