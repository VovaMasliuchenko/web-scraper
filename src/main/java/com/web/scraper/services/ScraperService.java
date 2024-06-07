package com.web.scraper.services;

import com.web.scraper.model.Product;
import com.web.scraper.repository.ProductRepository;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ScraperService {

    private final ChromeOptions chromeOptions;
    private final ProductRepository productRepository;

    public ScraperService(ChromeOptions chromeOptions, ProductRepository productRepository) {
        this.chromeOptions = chromeOptions;
        this.productRepository = productRepository;
    }

    public Product scrapeProduct(String productName) {
        return productRepository.findByNameContaining(productName).stream().findFirst().orElseGet(() -> {
            WebDriver webDriver = new ChromeDriver(chromeOptions);
            Product product = new Product();
            try {
                webDriver.get("https://epicentrk.ua/");

                WebElement searchBox = webDriver.findElement(By.xpath("//form[contains(@data-is, 'Search')]/input"));
                searchBox.sendKeys(productName);

                WebElement searchButton = webDriver.findElement(By.xpath("//button[@class='_cvO7u1']"));
                searchButton.click();

                switch (productName) {
                    case "плитка":
                        Thread.sleep(2000);
                        webDriver.findElement(By.xpath("(//li[@class='_ZIUN1N _EDdeGJ _OMtODJ _Nu8y3k']//a)[1]")).click();

                        Thread.sleep(2000);
                        webDriver.findElement(By.xpath("//li[@data-test-small-card='1']//a")).click();

                        Thread.sleep(2000);
                        product.setPrice(webDriver.findElement(By.xpath("(//div[@class='_tqVytn _Bfr6tl'])[1]")).getText());
                        List<WebElement> colorElements = webDriver.findElements(By.xpath("//a/div[@class='_mcvk-l _gBdcpi']"));
                        List<String> availableColors = new ArrayList<>();
                        for (WebElement colorElement:
                                colorElements) {
                            String style = colorElement.getAttribute("style");
                            int begin = style.indexOf('"');
                            int end = style.indexOf('"', begin + 1);
                            availableColors.add(style.substring(begin + 1, end));
                        }
                        product.setAvailableColors(availableColors);
                        product.setName(webDriver.findElement(By.xpath("//h1")).getText());
                        product.setUrl(webDriver.getCurrentUrl());
                        break;
                    case "фарба гумова":
                        Thread.sleep(2000);
                        webDriver.findElement(By.xpath("//li[@data-test-small-card='1']//a")).click();

                        Thread.sleep(2000);
                        product.setPrice(webDriver.findElement(By.xpath("(//div[@class='_tqVytn _Bfr6tl'])[1]")).getText());
                        webDriver.findElement(By.xpath("//div[@class='_swH4z2 _9j7it0 _M8Ya-E']")).click();
                        List<WebElement> paintColorElements = webDriver.findElements(By.xpath("//div[@class='_MS10aN']"));
                        List<String> paintAvailableColors = new ArrayList<>();
                        for (WebElement colorElement:
                                paintColorElements) {
                            String style = colorElement.getAttribute("style");
                            int begin = style.indexOf(':');
                            int end = style.indexOf(';');
                            paintAvailableColors.add(style.substring(begin + 2, end));
                        }
                        product.setAvailableColors(paintAvailableColors);
                        product.setName(webDriver.findElement(By.xpath("//h1")).getText());
                        product.setUrl(webDriver.getCurrentUrl());
                        break;
                    case "ламінат":
                        Thread.sleep(2000);
                        webDriver.findElement(By.xpath("//li[@data-test-small-card='1']//a")).click();

                        Thread.sleep(2000);
                        product.setPrice(webDriver.findElement(By.xpath("(//div[@class='_tqVytn _Bfr6tl'])[1]")).getText());
                        List<WebElement> floorColorElements = webDriver.findElements(By.xpath("//ul[@class='_hUaBPO']/li//img"));
                        List<String> floorAvailableColors = new ArrayList<>();
                        for (WebElement colorElement:
                                floorColorElements) {
                            String src = colorElement.getAttribute("src");
                            floorAvailableColors.add(src);
                        }
                        product.setAvailableColors(floorAvailableColors);
                        product.setName(webDriver.findElement(By.xpath("//h1")).getText());
                        product.setUrl(webDriver.getCurrentUrl());
                        break;
                }

            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                webDriver.quit();
            }
            productRepository.save(product);
            return product;
        });
    }
}
