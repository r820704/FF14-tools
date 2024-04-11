package com.ffxiv.crawler.service;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class CrawlerService {

  @Value("${ffxiv.house.url:}")
  private String HOUSE_LISTING_URL;
  @Value("${webdriver.chrome.driver}")
  private String WEBDRIVER_PATH;


  public static void main(String[] args) throws IOException, InterruptedException {

    CrawlerService wikiParser = new CrawlerService();
    ChromeOptions options = new ChromeOptions();
    options.addArguments("--remote-allow-origins=*");
    //		wikiParser.driver = new ChromeDriver(options);
    WebDriver driver = new RemoteWebDriver(new URL("http://localhost:4444"), options);

    wikiParser.getHouseList();

    driver.close();
  }

  public String getHouseList() {

    try {

      WebDriver driver = getWebDriver();

      log.info("目前的URL :" + driver.getCurrentUrl());

      // 如果當前URL已經進入house查詢畫面，則不重複get，否則會無法成功獲取畫面上elements
      if (!HOUSE_LISTING_URL.equals(driver.getCurrentUrl())) {
        driver.get(HOUSE_LISTING_URL);
        // 取得pageTitle
        String title = driver.getTitle();
        log.info("取得的title:" + title);
      }
      // fixme sometimes not work
      try {
        WebElement Boxelement = driver.findElement(By.className("info-box"));
        log.info("取得info-box元素");
        WebElement selectElement = Boxelement.findElement(By.cssSelector("select"));
        log.info("取得select元素");
        Select selector = new Select(selectElement);
        selector.selectByValue("1178");
        log.info("取得1178元素");

        WebElement submitButton = driver.findElement(By.className("is-primary"));
        log.info("取得is-primary元素");

//        // 使用 WebDriverWait 來等待提交按鈕變為可點選狀態
//        WebDriverWait wait = new  WebDriverWait(driver, Duration.ofSeconds(10));
//        wait.until(ExpectedConditions.elementToBeClickable(submitButton));

        submitButton.click();
      } catch (ElementNotInteractableException e) {
        log.info("已執行過查詢所以跳過初次選擇伺服器視窗動作");
      }

      //implicitlyWait似乎會套用到全部selenium的操作
      driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(2));
      List<WebElement> saleItems = driver.findElements(By.className("sale-item"));
      log.info("==========開始列印============");

      StringBuilder houseList = new StringBuilder();
      TreeMap houseMap = new TreeMap<String, Map>();

      houseList.append("以下是僅供個人購買的房屋列表: \n ");

      int count = 0;
      for (int i = 0; i < saleItems.size(); i++) {
        log.info("總共有" + saleItems.size() + "項");
        log.info("目前是第幾項: " + (i + 1));
        //		    WebElement item = driver.findElements(By.className("sale-item")).get(i);
        WebElement item = saleItems.get(i);

        try {
          WebElement span = item.findElement(By.cssSelector("span[data-tooltip='仅供个人购买']"));
        } catch (NoSuchElementException e) {
          continue;
        } catch (StaleElementReferenceException e) {
          continue;
        }
        count++;
        String[] itemContent = item.getText().split("\n");
        String houseRegion = itemContent[0]; // ex: 海雾村 公馆 (M)
        String houseNum_Price = itemContent[1]; // ex: 3 区 7 号 16,000,000
        String houseStatus = itemContent[3]; // ex: 即将开始抽签预约！
        String beginOrEndTime = itemContent[4]; // ex: (推测数据)09-22 23:00 开始
        String updateTime = itemContent[5]; // ex: 2022-09-16 23:02:54 更新
        String houseResult = "  " + houseNum_Price + ", " + beginOrEndTime + "\n";

        //		    houseList.append(itemContent[0] + ", " + itemContent[1] + ", " + itemContent[3] + ",
        // "
        //  + itemContent[4] + ", "
        //		    		 + itemContent[5] + "\n");

        if (houseMap.containsKey(houseStatus)) {
          TreeMap<String, StringBuilder> resultMap =
              (TreeMap<String, StringBuilder>) houseMap.get(houseStatus);
          if (resultMap.containsKey(houseRegion)) {
            StringBuilder tempList = (StringBuilder) resultMap.get(houseRegion);
            tempList.append(houseResult);
          } else {
            resultMap.put(houseRegion, new StringBuilder(houseResult));
          }
        } else {
          TreeMap<String, StringBuilder> resultMap = new TreeMap<String, StringBuilder>();
          resultMap.put(houseRegion, new StringBuilder(houseResult));
          houseMap.put(houseStatus, resultMap);
        }
      }
      Set<Map.Entry<String, Map>> entrySet = houseMap.entrySet();
      for (Map.Entry<String, Map> entry : entrySet) {
        houseList.append(entry.getKey() + "分類如下" + "\n");
        Set<String> keyset = entry.getValue().keySet();
        for (String s : keyset) {
          houseList.append(s + "\n");
          houseList.append(entry.getValue().get(s) + "\n");
        }
      }

      log.info("houseList = " + houseList);
      log.info("總共有:" + count + "項");
      driver.quit();
      log.info("關閉webdriver");
      return houseList.toString();
    } catch (MalformedURLException e) {
      log.error("getHouseList 失敗");
    }
    return "getHouseList 失敗";
  }

  public WebDriver getWebDriver() throws MalformedURLException {
    // fixme 在初始化後，在查詢過一次(執行了driver.quit())，或是甚麼都不做過了大約五分鐘，session都會timeout關閉，導致下一次的查詢要使用舊Session時失敗
    // fixme 應要每次使用時都重新初始化driver
    // fixme
    // (https://stackoverflow.com/questions/69253844/proper-driver-quit-to-enable-reopening-the-driver
    log.info("WEBDRIVER_PATH: " + WEBDRIVER_PATH);
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

    WebDriver driver = new RemoteWebDriver(new URL(WEBDRIVER_PATH), options, false);
    log.info("REMOTE WEBDRIVER建立成功");
    return driver;
  }
}
