package com.ff14.crawler;

import java.io.IOException;
import java.time.Duration;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.ElementNotInteractableException;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WikiParser {
	
	@Autowired
	private WebDriver driver;
	
	public String getHouseList() throws InterruptedException {
		
		System.out.println("目前的URL :" + driver.getCurrentUrl());
		
		// 如果當前URL已經進入house查詢畫面，則不重複get，否則會無法成功獲取畫面上elements
		if(!"https://house.ffxiv.cyou/#/".equals(driver.getCurrentUrl())) {
			driver.get("https://house.ffxiv.cyou/#/");
			// 取得pageTitle
			String title = driver.getTitle();			
		}

//		WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(3L));
//		wait.until(ExpectedConditions.presenceOfElementLocated(By.className("info-box")));

//Thread.sleep(3000);
		
		
		try {
			WebElement Boxelement = driver.findElement(By.className("info-box"));
			WebElement selectElement = Boxelement.findElement(By.cssSelector("select"));
			Select selector = new Select(selectElement);
			selector.selectByValue("1178");
//		System.out.println(selector.getText());
			
			WebElement submitButton = driver.findElement(By.className("is-primary"));
			submitButton.click();			
		} catch(ElementNotInteractableException e) {
			System.out.println("已執行過查詢所以跳過初次選擇伺服器視窗動作");
		}
		
		
		List<WebElement> saleItems = driver.findElements(By.className("sale-item"));
		System.out.println("==========開始列印============");
//		System.out.println("購買對象: " + saleItems.get(0).findElement
//				(By.cssSelector("span[data-tooltip='仅供部队购买']")).getDomAttribute("data-tooltip"));
		
		StringBuilder houseList = new StringBuilder();
		
//		Thread.sleep(1000);
		
		houseList.append("以下是僅供個人購買的房屋列表\n : ");
		
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
		    houseList.append(itemContent[0] + ", " + itemContent[1] + ", " + itemContent[3] + ", "  + itemContent[4] + ", "
		    		 + itemContent[5] + "\n");
		}
		
		System.out.println("houseList = " + houseList);
		System.out.println(count);
		return houseList.toString();
		
	}
	
	
	
	public static void main(String[] args) throws IOException, InterruptedException {
		
		System.setProperty("webdriver.chrome.driver", "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chromedriver.exe");
		
		WikiParser wikiParser = new WikiParser();
		wikiParser.driver = new ChromeDriver();
		
		wikiParser.getHouseList();
		
//		wikiParser.driver.close();

	}
	
}
