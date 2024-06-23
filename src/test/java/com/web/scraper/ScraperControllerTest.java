package com.web.scraper;

import com.web.scraper.controller.ScraperController;
import com.web.scraper.model.Product;
import com.web.scraper.services.ClusteringService;
import com.web.scraper.services.ProductService;
import com.web.scraper.services.ScraperService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ScraperController.class)
public class ScraperControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ScraperService scraperService;

    @MockBean
    private ProductService productService;

    @MockBean
    private ClusteringService clusteringService;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testVisualizeClustering() throws Exception {
        List<Product> products = Arrays.asList(new Product(), new Product());
        when(productService.getAllProducts()).thenReturn(products);
        doNothing().when(clusteringService).visualizeClustering(anyList(), anyInt(), anyString(), any());

        mockMvc.perform(get("/api/scraper/visualize-clustering")
                        .param("clusterCount", "3")
                        .param("parameters", "price", "rating"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG));
    }

    @Test
    public void testStartScraping() throws Exception {
        doNothing().when(scraperService).scrapeProduct(anyString());

        mockMvc.perform(get("/api/scraper/start-scraping")
                        .param("keyword", "плитка"))
                .andExpect(status().isOk());
    }

    @Test
    public void testGetLowPriceProducts() throws Exception {
        List<Product> products = Arrays.asList(new Product(), new Product());
        when(productService.getLowPriceProducts(anyInt(), anyString())).thenReturn(products);

        mockMvc.perform(get("/api/scraper/products/low-price")
                        .param("clusterCount", "3"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testGetHighPriceProducts() throws Exception {
        List<Product> products = Arrays.asList(new Product(), new Product());
        when(productService.getHighPriceProducts(anyInt(), anyString())).thenReturn(products);

        mockMvc.perform(get("/api/scraper/products/high-price")
                        .param("clusterCount", "3"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testGetLowRatingProducts() throws Exception {
        List<Product> products = Arrays.asList(new Product(), new Product());
        when(productService.getLowRatingProducts(anyInt(), anyString())).thenReturn(products);

        mockMvc.perform(get("/api/scraper/products/low-rating")
                        .param("clusterCount", "3"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testGetHighRatingProducts() throws Exception {
        List<Product> products = Arrays.asList(new Product(), new Product());
        when(productService.getHighRatingProducts(anyInt(), anyString())).thenReturn(products);

        mockMvc.perform(get("/api/scraper/products/high-rating")
                        .param("clusterCount", "3"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    public void testGetLowPriceHighRatingProducts() throws Exception {
        List<Product> products = Arrays.asList(new Product(), new Product());
        when(productService.getLowPriceHighRatingProducts(anyInt(), anyString())).thenReturn(products);

        mockMvc.perform(get("/api/scraper/products/low-price-high-rating")
                        .param("clusterCount", "3"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }
}
