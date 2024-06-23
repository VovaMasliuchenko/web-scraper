package com.web.scraper;

import com.web.scraper.model.Product;
import com.web.scraper.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
public class ProductRepositoryTest {
    @Autowired
    private ProductRepository productRepository;

    @Test
    public void testFindByNameContaining() {
        Product product = new Product();
        product.setName("Ламінат");
        productRepository.save(product);

        List<Product> result = productRepository.findByNameContaining("Ламінат");

        assertNotEquals(0, result.size());
        assertTrue(result.get(0).getName().contains("Ламінат"));
    }

    @Test
    public void testSaveAndFindById() {
        Product product = new Product();
        product.setName("Ламінат");
        product = productRepository.save(product);

        Optional<Product> result = productRepository.findById(product.getId());

        assertTrue(result.isPresent());
        assertTrue(result.get().getName().contains("Ламінат"));
    }
}
