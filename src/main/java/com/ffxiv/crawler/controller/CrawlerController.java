package com.ffxiv.crawler.controller;

import com.ffxiv.crawler.service.CrawlerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/crawler")
@RestController
public class CrawlerController {

  @Autowired public CrawlerService crawlerService;

  @GetMapping("/getcrawlerresult")
  public String getcrawlerresult() {

    return crawlerService.getHouseList();
  }
}
