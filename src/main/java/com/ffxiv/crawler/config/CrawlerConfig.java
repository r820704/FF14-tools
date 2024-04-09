package com.ffxiv.crawler.config;

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
@ComponentScan(basePackages = {"com.ffxiv"})
public class CrawlerConfig {

  @Value("${webdriver.chrome.driver}")
  private String WEBDRIVER_PATH;

  @Bean(destroyMethod = "quit")
  //	@Scope("prototype")
  public WebDriver driver(@Value("${webdriver.chrome.driver}") String WEBDRIVER_PATH)
      throws MalformedURLException {
    // fixme 在初始化後，在查詢過一次(執行了driver.quit())，或是甚麼都不做過了大約五分鐘，session都會timeout關閉，導致下一次的查詢要使用舊Session時失敗
    // fixme 應要每次使用時都重新初始化driver
    // fixme (https://stackoverflow.com/questions/69253844/proper-driver-quit-to-enable-reopening-the-driver
    System.out.println("WEBDRIVER_PATH: " + WEBDRIVER_PATH);
    // 若瀏覽器安裝位置為預設則webDriver會自動搜尋path設定的位置，也可以使用System.setProperty 來指定路徑
    System.setProperty("webdriver.chrome.driver", WEBDRIVER_PATH);
    // Selenium對不同瀏覽器提供了不同的webDriver

    // 設定chromeDriver不要開啟Gui，使用無頭模式，要加上此段才能在linux環境順利產生ChromeDriver
    ChromeOptions options = new ChromeOptions();
    //        options.addArguments("--headless","--disable-gpu","--remote-allow-origins=*");  //
    // windows環境目前能正常啟動的參數
    options.addArguments(
        "--headless",
        "--no-sandbox",
        "--disable-dev-shm-usage",
        "--remote-allow-origins=*"); // linux環境目前能正常啟動的參數

    WebDriver driver = new RemoteWebDriver(new URL(WEBDRIVER_PATH), options);
    System.out.println("WEBDRIVER_PATH建立成功");
    return driver;
  }
}
