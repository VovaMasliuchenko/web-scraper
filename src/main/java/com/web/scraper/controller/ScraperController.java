package com.web.scraper.controller;

import com.web.scraper.model.Product;
import com.web.scraper.services.ScraperService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://localhost:8081")
@RestController
@RequestMapping("/api/scraper")
public class ScraperController {

	private final ScraperService scraperService;

	@Autowired
	public ScraperController(ScraperService scraperService) {
		this.scraperService = scraperService;
	}

	@CrossOrigin(origins = "http://localhost:3000")
	@GetMapping("/product")
	public ResponseEntity<Product> scrapeProduct(@RequestParam("productName") String productName) {
		try {
			Product product = scraperService.scrapeProduct(productName);
			return new ResponseEntity<>(product, HttpStatus.OK);
		} catch (Exception e) {
			return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}

	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public String handleException(Exception e) {
		return e.getMessage();
	}

}
