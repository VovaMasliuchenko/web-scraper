package com.web.scraper.services;

import com.web.scraper.model.Product;
import com.web.scraper.repository.ProductRepository;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ProductService {

    private final ScraperService scraperService;
    private final ClusteringService clusteringService;
    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ScraperService scraperService, ClusteringService clusteringService, ProductRepository productRepository) {
        this.scraperService = scraperService;
        this.clusteringService = clusteringService;
        this.productRepository = productRepository;
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getProductsInCluster(int clusterId, String productKeyword, String... parameters) {
        List<Product> allProducts = productRepository.findByKeyword(productKeyword);
        List<CentroidCluster<ProductClusterable>> clusters = clusteringService.performClustering(allProducts, 3, parameters);

        if (clusterId >= clusters.size()) {
            throw new IllegalArgumentException("Cluster not found");
        }

        Cluster<ProductClusterable> selectedCluster = clusters.get(clusterId);

        List<Product> productsInCluster = new ArrayList<>();
        for (ProductClusterable productClusterable : selectedCluster.getPoints()) {
            productsInCluster.add(productClusterable.getProduct());
        }

        for (Product product : productsInCluster) {
            scraperService.updateProduct(product);
        }

        return productsInCluster;
    }

    public List<String> getClusterIds(String... parameters) {
        List<Product> allProducts = productRepository.findAll();
        return clusteringService.getClusterIds(allProducts, 3, parameters);
    }

    public List<Product> getLowPriceHighRatingProducts(int clusterCount, String productKeyword) {
        List<Product> allProducts = productRepository.findByKeyword(productKeyword);
        List<Product> products = clusteringService.getLowPriceHighRatingProducts(allProducts, clusterCount);

        for (Product product : products) {
            scraperService.updateProduct(product);
        }

        return products;
    }

    public List<Product> getLowPriceProducts(int clusterCount, String productKeyword) {
        List<Product> allProducts = productRepository.findByKeyword(productKeyword);
        List<Product> products = clusteringService.getLowPriceProducts(allProducts, clusterCount);

        for (Product product : products) {
            scraperService.updateProduct(product);
        }

        return products;
    }

    public List<Product> getHighPriceProducts(int clusterCount, String productKeyword) {
        List<Product> allProducts = productRepository.findByKeyword(productKeyword);
        List<Product> products = clusteringService.getHighPriceProducts(allProducts, clusterCount);

        for (Product product : products) {
            scraperService.updateProduct(product);
        }

        return products;
    }

    public List<Product> getLowRatingProducts(int clusterCount, String productKeyword) {
        List<Product> allProducts = productRepository.findByKeyword(productKeyword);
        List<Product> products = clusteringService.getLowRatingProducts(allProducts, clusterCount);

        for (Product product : products) {
            scraperService.updateProduct(product);
        }

        return products;
    }

    public List<Product> getHighRatingProducts(int clusterCount, String productKeyword) {
        List<Product> allProducts = productRepository.findByKeyword(productKeyword);
        List<Product> products = clusteringService.getHighRatingProducts(allProducts, clusterCount);

        for (Product product : products) {
            scraperService.updateProduct(product);
        }

        return products;
    }
}