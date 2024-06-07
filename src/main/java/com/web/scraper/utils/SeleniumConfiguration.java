package com.web.scraper.utils;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SeleniumConfiguration {
    @Bean
    public ChromeOptions chromeOptions() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless"); // Запуск в режимі headless
        return options;
    }

    @Bean
    public WebDriver webDriver(ChromeOptions options) {
        System.setProperty("webdriver.chrome.driver", "D:\\Selenium\\chromedriver.exe");
        return new ChromeDriver(options);
    }
}
