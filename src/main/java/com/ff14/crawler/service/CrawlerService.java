package com.ff14.crawler.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.Select;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class CrawlerService {
	
	@Autowired
	@Lazy
	private WebDriver driver;
	
	public String getHouseList() throws InterruptedException {
		
		System.out.println("目前的URL :" + driver.getCurrentUrl());
		
		// 如果當前URL已經進入house查詢畫面，則不重複get，否則會無法成功獲取畫面上elements
		if(!"https://house.ffxiv.cyou/#/".equals(driver.getCurrentUrl())) {
			driver.get("https://house.ffxiv.cyou/#/");
			// 取得pageTitle
			String title = driver.getTitle();			
		}

		try {
			WebElement Boxelement = driver.findElement(By.className("info-box"));
			WebElement selectElement = Boxelement.findElement(By.cssSelector("select"));
			Select selector = new Select(selectElement);
			selector.selectByValue("1178");
			
			WebElement submitButton = driver.findElement(By.className("is-primary"));
			submitButton.click();			
		} catch(ElementNotInteractableException e) {
			System.out.println("已執行過查詢所以跳過初次選擇伺服器視窗動作");
		}
		
		
		List<WebElement> saleItems = driver.findElements(By.className("sale-item"));
		System.out.println("==========開始列印============");
		
		StringBuilder houseList = new StringBuilder();
		TreeMap houseMap = new TreeMap<String, Map>();
		
		
		houseList.append("以下是僅供個人購買的房屋列表: \n ");
		
int count = 0;
		for (int i = 0; i < saleItems.size(); i++) {
System.out.println("總共有" + saleItems.size() + "項");
System.out.println("目前是第幾項: " + (i+1));			
//		    WebElement item = driver.findElements(By.className("sale-item")).get(i);
		    WebElement item = saleItems.get(i);
		    
		    try {
		    	WebElement span = item.findElement(By.cssSelector("span[data-tooltip='仅供个人购买']"));
		    } catch(NoSuchElementException e) {
		    	continue;
		    } catch(StaleElementReferenceException e) {
		    	continue;
		    }
		    count++;
		    String[] itemContent = item.getText().split("\n");
		    String houseRegion = itemContent[0] ;  // ex: 海雾村 公馆 (M)
		    String houseNum_Price = itemContent[1] ; // ex: 3 区 7 号 16,000,000
		    String houseStatus = itemContent[3] ; // ex: 即将开始抽签预约！
		    String beginOrEndTime = itemContent[4]; // ex: (推测数据)09-22 23:00 开始
		    String updateTime = itemContent[5]; // ex: 2022-09-16 23:02:54 更新
		    String houseResult = "  "  + houseNum_Price + ", "  + beginOrEndTime
		    		 + "\n" ;
		    

//		    houseList.append(itemContent[0] + ", " + itemContent[1] + ", " + itemContent[3] + ", "  + itemContent[4] + ", "
//		    		 + itemContent[5] + "\n");

		    if(houseMap.containsKey(houseStatus)) {
		    	TreeMap<String, StringBuilder> resultMap = (TreeMap<String, StringBuilder>) houseMap.get(houseStatus);
		    	if(resultMap.containsKey(houseRegion)) {
		    		StringBuilder tempList = (StringBuilder) resultMap.get(houseRegion);
		    		tempList.append(houseResult);		    		
		    	}else {
		    		resultMap.put(houseRegion, new StringBuilder(houseResult));
		    	}
		    }else {
		    	TreeMap<String, StringBuilder> resultMap = new TreeMap<String, StringBuilder>();
		    	resultMap.put(houseRegion, new StringBuilder(houseResult));
		    	houseMap.put(houseStatus, resultMap);
		    }
		}
		Set<Map.Entry<String, Map>> entrySet = houseMap.entrySet() ;
		for(Map.Entry<String, Map> entry : entrySet) {
			houseList.append(entry.getKey() + "分類如下" + "\n");
			Set<String> keyset = entry.getValue().keySet();
			for(String s : keyset) {
				houseList.append(s + "\n");
				houseList.append(entry.getValue().get(s) + "\n");
			}
		}
		
		
		
		System.out.println("houseList = " + houseList);
		System.out.println("總共有:" + count +"項");
		return houseList.toString();
		
	}
	
	
	
	public static void main(String[] args) throws IOException, InterruptedException {
		
		System.setProperty("webdriver.chrome.driver", "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chromedriver.exe");
		
		CrawlerService wikiParser = new CrawlerService();
		ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");   
		wikiParser.driver = new ChromeDriver(options);
		
		wikiParser.getHouseList();
		
		wikiParser.driver.close();

	}
	
}
