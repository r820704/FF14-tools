package com.ffxiv.crawler.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.ffxiv.crawler.service.CrawlerService;

@RequestMapping("/crawler")
@RestController
public class CrawlerController {

	@Autowired
	public CrawlerService crawlerService;
	
	@GetMapping("/getcrawlerresult")
	public String getcrawlerresult() throws InterruptedException {
		
		return crawlerService.getHouseList();
	}
}
