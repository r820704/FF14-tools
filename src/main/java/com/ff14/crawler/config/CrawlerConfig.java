package com.ff14.crawler.config;

import java.net.MalformedURLException;
import java.net.URL;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"com.ff14"})
public class CrawlerConfig {
	
	@Value("${webdriver.chrome.driver}")
	private String WEBDRIVER_PATH ;
	
	@Bean(destroyMethod = "quit")
	public WebDriver driver(@Value("${webdriver.chrome.driver}") String WEBDRIVER_PATH) throws MalformedURLException {

		System.out.println("WEBDRIVER_PATH: " + WEBDRIVER_PATH);
		//若瀏覽器安裝位置為預設則webDriver會自動搜尋path設定的位置，也可以使用System.setProperty 來指定路徑
		System.setProperty("webdriver.chrome.driver", WEBDRIVER_PATH);
		//Selenium對不同瀏覽器提供了不同的webDriver
		
		// 設定chromeDriver不要開啟Gui，使用無頭模式，要加上此段才能在linux環境順利產生ChromeDriver
        ChromeOptions options = new ChromeOptions();
//        options.addArguments("--headless","--disable-gpu","--remote-allow-origins=*");  // windows環境目前能正常啟動的參數 
        options.addArguments("--headless","--no-sandbox","--disable-dev-shm-usage","--remote-allow-origins=*"); // linux環境目前能正常啟動的參數 
        
        WebDriver driver = new RemoteWebDriver(new URL(WEBDRIVER_PATH), options);
        return driver;
	}
		
}
