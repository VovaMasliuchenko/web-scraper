package com.web.scraper;

import com.web.scraper.model.Product;
import com.web.scraper.repository.ProductRepository;
import com.web.scraper.services.ScraperService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeOptions;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class ScraperServiceTest {
    @Mock
    private ChromeOptions chromeOptions;

    @Mock
    private WebDriver webDriver;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ScraperService scraperService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testScrapeProductWhenProductExists() {
        String productName = "Ламінат";
        Product product = new Product();
        product.setName(productName);
        when(productRepository.findByNameContaining(any(String.class))).thenReturn(List.of(product));

        List<Product> result = scraperService.scrapeProduct(productName);

        assertNotEquals(0, result.size());
        verify(productRepository, times(1)).findByNameContaining(productName);
        verify(productRepository, never()).saveAll(any());
    }

    @Test
    public void testScrapeProductWhenProductDoesNotExist() {
        String productName = "Ламінат";
        when(productRepository.findByNameContaining(any(String.class))).thenReturn(new ArrayList<>());

        List<Product> result = scraperService.scrapeProduct(productName);

        assertNotEquals(0, result.size());
        verify(productRepository, times(1)).findByNameContaining(productName);
        verify(productRepository, times(1)).saveAll(any());
    }
}
