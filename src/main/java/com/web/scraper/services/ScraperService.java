package com.web.scraper.services;

import com.web.scraper.model.AvailableColor;
import com.web.scraper.model.Product;
import com.web.scraper.repository.AvailableColorRepository;
import com.web.scraper.repository.ProductRepository;
import org.openqa.selenium.By;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class ScraperService {

    private final ChromeOptions chromeOptions;
    private final ProductRepository productRepository;
    private final AvailableColorRepository availableColorRepository;

    @Autowired
    public ScraperService(ChromeOptions chromeOptions, ProductRepository productRepository, AvailableColorRepository availableColorRepository) {
        this.chromeOptions = chromeOptions;
        this.productRepository = productRepository;
        this.availableColorRepository = availableColorRepository;
    }

    public List<Product> scrapeProduct(String productName) {
        List<Product> productList = productRepository.findByKeyword(productName);
        if (productList.isEmpty() || productList.size() < 10) {
            WebDriver webDriver = new ChromeDriver(chromeOptions);
            WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(10));
            try {
                webDriver.get("https://epicentrk.ua/");

                WebElement searchBox = webDriver.findElement(By.xpath("//form[contains(@data-is, 'Search')]/input"));
                searchBox.sendKeys(productName);

                WebElement searchButton = webDriver.findElement(By.xpath("//button[@class='_cvO7u1']"));
                searchButton.click();

                        List<String> productLinks = webDriver.findElements(By.xpath("//li[@data-test-small-card]//div/a"))
                                .stream()
                                .limit(10)
                                .map(webElement -> webElement.getAttribute("href"))
                                .toList();
                        for (String productLink : productLinks) {
                            webDriver.navigate().to(productLink);
                            Thread.sleep(2000);

                            String productNameFromPage = webDriver.findElement(By.xpath("//h1")).getText();
                            boolean isProductPresent = productRepository.findByName(productNameFromPage).isPresent();
                            if (isProductPresent) continue;

                            Product product = new Product();
                            product.setKeyword(productName);
                            product.setName(productNameFromPage);
                            product.setPrice(webDriver.findElement(By.xpath("(//div[contains(@class, '_tqVytn')])[1]")).getText());
                            product.setUrl(webDriver.getCurrentUrl());
                            System.out.println(webDriver.getCurrentUrl());

                            List<WebElement> colorElements;

                            try {
                                colorElements = webDriver.findElements(By.xpath("//a/div[@class='_mcvk-l _gBdcpi']"));
                                if (colorElements.size() == 0) throw new NoSuchElementException("There are no elements!");
                            } catch (NoSuchElementException e) {
                                colorElements = webDriver.findElements(By.xpath("(//div[@class='swiper-zoom-container _xc4kSC']/img)[1]"));
                            }

                            Set<AvailableColor> availableColors = new HashSet<>();
                            for (WebElement colorElement : colorElements) {
                                String color;
                                wait.until(attributeToBeNotEmpty(colorElement, "style", "src"));

                                if (colorElement.getAttribute("style").equals("")) {
                                    color = colorElement.getAttribute("src");
                                } else {
                                    String style = colorElement.getAttribute("style");
                                    int begin = style.indexOf('"');
                                    int end = style.indexOf('"', begin + 1);
                                    color = style.substring(begin + 1, end);
                                }

                                AvailableColor availableColor = availableColorRepository.findByColor(color)
                                        .orElseGet(() -> availableColorRepository.save(new AvailableColor(color)));
                                availableColors.add(availableColor);
                            }
                            product.setAvailableColors(availableColors);
                            productList.add(product);
                        }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                webDriver.quit();
            }
            productRepository.saveAll(productList);

        } else {
            System.out.println("Product list already contains sufficient items or products for the given name.");
        }
        return productList;
    }

    private ExpectedCondition<Boolean> attributeToBeNotEmpty(final WebElement element, final String attribute1, final String attribute2) {
        return new ExpectedCondition<Boolean>() {
            public Boolean apply(WebDriver driver) {
                String attrValue1 = element.getAttribute(attribute1);
                String attrValue2 = element.getAttribute(attribute2);
                return (attrValue1 != null && !attrValue1.isEmpty()) || (attrValue2 != null && !attrValue2.isEmpty());
            }

            @Override
            public String toString() {
                return String.format("either attribute '%s' or '%s' to be not empty for element %s", attribute1, attribute2, element);
            }
        };
    }
}
