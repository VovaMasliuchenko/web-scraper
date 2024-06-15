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
import java.util.*;
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
        if (productList.isEmpty() || productList.size() < 20) {
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
                                .limit(20)
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

                            List<WebElement> starElements = webDriver.findElements(By.xpath("//div[@class='_tpM6VY _OMtODJ _oZRAns']//div[@class='_H03nLI _DlcWBu']"));
                            int rating = 0;
                            for (WebElement starElement : starElements) {
                                String style = starElement.getAttribute("style");
                                if (style != null && style.contains("rgb(255, 195, 66)")) {
                                    rating++;
                                }
                            }
                            product.setRating(rating == 0 ? 3 : rating); // за замовчуванням рейтинг 3 якщо нема відгуків

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

    public Product updateProduct(Product product) {
        if (product.getLastUpdated() != null && wasUpdatedInLast24Hours(product.getLastUpdated())) {
            System.out.println("Product was updated in the last 24 hours. Skipping update.");
            return product;
        }
        Calendar calendar = Calendar.getInstance();
        WebDriver webDriver = new ChromeDriver(chromeOptions);
        WebDriverWait wait = new WebDriverWait(webDriver, Duration.ofSeconds(10));
        try {
            webDriver.navigate().to(product.getUrl());
            Thread.sleep(2000);
            String productNameFromPage = webDriver.findElement(By.xpath("//h1")).getText();
            WebElement priceElement = wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("(//div[contains(@class, '_tqVytn')])[1]")));
            product.setName(productNameFromPage);
            product.setPrice(priceElement.getText());
            List<WebElement> starElements = webDriver.findElements(By.xpath("//div[@class='_tpM6VY _OMtODJ _oZRAns']//div[@class='_H03nLI _DlcWBu']"));
            int rating = 0;
            for (WebElement starElement : starElements) {
                String style = starElement.getAttribute("style");
                if (style != null && style.contains("rgb(255, 195, 66)")) {
                    rating++;
                }
            }
            product.setRating(rating == 0 ? 3 : rating);
            product.setLastUpdated(calendar.getTime());

            productRepository.save(product);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            webDriver.quit();
        }
        return product;
    }

    private boolean wasUpdatedInLast24Hours(Date lastUpdated) {
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.HOUR, -24);
        Date twentyFourHoursAgo = calendar.getTime();
        return lastUpdated.after(twentyFourHoursAgo);
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
