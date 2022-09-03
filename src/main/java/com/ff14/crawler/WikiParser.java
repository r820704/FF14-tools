package com.ff14.crawler;

import java.io.IOException;
import java.util.List;

import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;

public class WikiParser {
	
	public static void main(String[] args) throws IOException, InterruptedException {
		
		
		//若瀏覽器安裝位置為預設則webDriver會自動搜尋path設定的位置，也可以使用System.setProperty 來指定路徑
		System.setProperty("webdriver.chrome.driver", "C:\\Program Files (x86)\\Google\\Chrome\\Application\\chromedriver.exe");
		//Selenium對不同瀏覽器提供了不同的webDriver
		WebDriver driver = new ChromeDriver(); // googleChrome

		driver.get("https://house.ffxiv.cyou/#/");
		// 取得pageTitle
		String title = driver.getTitle();
//		System.out.print(title);
		
		WebElement Boxelement = driver.findElement(By.className("info-box"));
		WebElement selectElement = Boxelement.findElement(By.cssSelector("select"));
		Select selector = new Select(selectElement);
		selector.selectByValue("1178");
//		System.out.println(selector.getText());
		
		WebElement submitButton = driver.findElement(By.className("is-primary"));
		submitButton.click();
		
		Thread.sleep(3000);
		
		List<WebElement> saleItems = driver.findElements(By.className("sale-item"));
		System.out.println("==========開始列印============");
		System.out.println("購買對象: " + saleItems.get(0).findElement
				(By.cssSelector("span[data-tooltip='仅供部队购买']")).getDomAttribute("data-tooltip"));
		
		StringBuilder houseList = new StringBuilder();
		
		for (int i = 0; i < saleItems.size(); i++) {
		    WebElement item = driver.findElements(By.className("sale-item")).get(i);
		    
		    try {
		    	WebElement span = item.findElement(By.cssSelector("span[data-tooltip='仅供个人购买']"));
		    } catch(NoSuchElementException e) {
		    	continue;
		    }
		    String[] itemContent = item.getText().split("\n");
		    houseList.append(itemContent[0] + ", " + itemContent[1] + ", " + itemContent[3] + ", "  + itemContent[4] + ", "
		    		 + itemContent[5] + "\n");
		}
		
		System.out.println("houseList = " + houseList);
		
	}
	
}
