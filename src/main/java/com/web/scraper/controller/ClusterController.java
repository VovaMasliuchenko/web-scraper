package com.web.scraper.controller;

import com.web.scraper.model.Product;
import com.web.scraper.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clusters")
public class ClusterController {

    private final ProductService productService;

    @Autowired
    public ClusterController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/{clusterId}")
    public ResponseEntity<List<Product>> getProductsInCluster(@PathVariable int clusterId, @RequestParam String productKeyword, @RequestParam String[] parameters) {
        List<Product> products = productService.getProductsInCluster(clusterId, productKeyword, parameters);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/ids")
    public ResponseEntity<List<String>> getClusterIds(@RequestParam String[] parameters) {
        List<String> clusterIds = productService.getClusterIds(parameters);
        return ResponseEntity.ok(clusterIds);
    }

    @GetMapping("/low-price-high-rating")
    public ResponseEntity<List<Product>> getLowPriceHighRatingProducts(@RequestParam int clusterCount, @RequestParam String productKeyword) {
        List<Product> products = productService.getLowPriceHighRatingProducts(clusterCount, productKeyword);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/low-price")
    public ResponseEntity<List<Product>> getLowPriceProducts(@RequestParam int clusterCount, @RequestParam String productKeyword) {
        List<Product> products = productService.getLowPriceProducts(clusterCount, productKeyword);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/high-price")
    public ResponseEntity<List<Product>> getHighPriceProducts(@RequestParam int clusterCount, @RequestParam String productKeyword) {
        List<Product> products = productService.getHighPriceProducts(clusterCount, productKeyword);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/low-rating")
    public ResponseEntity<List<Product>> getLowRatingProducts(@RequestParam int clusterCount, @RequestParam String productKeyword) {
        List<Product> products = productService.getLowRatingProducts(clusterCount, productKeyword);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/high-rating")
    public ResponseEntity<List<Product>> getHighRatingProducts(@RequestParam int clusterCount, @RequestParam String productKeyword) {
        List<Product> products = productService.getHighRatingProducts(clusterCount, productKeyword);
        return ResponseEntity.ok(products);
    }
}