package com.web.scraper.controller;

import com.web.scraper.model.Product;
import com.web.scraper.services.ClusteringService;
import com.web.scraper.services.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/clusters")
public class ClusterController {

    private final ProductService productService;
    private final ClusteringService clusteringService;

    @Autowired
    public ClusterController(ProductService productService, ClusteringService clusteringService) {
        this.productService = productService;
        this.clusteringService = clusteringService;
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

    @GetMapping("/visualize-clustering")
    public ResponseEntity<InputStreamResource> visualizeClustering(@RequestParam int clusterCount, @RequestParam String... parameters) throws IOException {
        List<Product> products = productService.getAllProducts();
        String outputFile = "clustering.png";
        clusteringService.visualizeClustering(products, clusterCount, outputFile, parameters);

        File file = new File(outputFile);
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=clustering.png");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.IMAGE_PNG)
                .body(resource);
    }

    @GetMapping("/visualize-elbow-method")
    public ResponseEntity<InputStreamResource> visualizeElbowMethod(@RequestParam int maxClusters, @RequestParam String... parameters) throws IOException {
        List<Product> products = productService.getAllProducts();
        String outputFile = "elbow_method.png";
        clusteringService.visualizeElbowMethod(products, maxClusters, outputFile, parameters);

        File file = new File(outputFile);
        InputStreamResource resource = new InputStreamResource(new FileInputStream(file));

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=elbow_method.png");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.IMAGE_PNG)
                .body(resource);
    }
}