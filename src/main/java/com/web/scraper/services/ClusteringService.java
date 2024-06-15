package com.web.scraper.services;

import com.web.scraper.model.Product;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
public class ClusteringService {

    public List<CentroidCluster<ProductClusterable>> performClustering(List<Product> products, int clusterCount, String... parameters) {
        List<ProductClusterable> points = new ArrayList<>();
        for (Product product : products) {
            double[] point = new double[parameters.length];
            for (int i = 0; i < parameters.length; i++) {
                switch (parameters[i]) {
                    case "price":
                        point[i] = parsePrice(product.getPrice());
                        break;
                    case "rating":
                        point[i] = product.getRating();
                        break;
                    default:
                        throw new IllegalArgumentException("Unknown parameter: " + parameters[i]);
                }
            }
            points.add(new ProductClusterable(point, product));
        }

        KMeansPlusPlusClusterer<ProductClusterable> clusterer = new KMeansPlusPlusClusterer<>(clusterCount);
        return clusterer.cluster(points);
    }

    public List<Product> getLowPriceProducts(List<Product> products, int clusterCount) {
        return getProductsByCriterion(products, clusterCount, "price", true);
    }

    public List<Product> getHighPriceProducts(List<Product> products, int clusterCount) {
        return getProductsByCriterion(products, clusterCount, "price", false);
    }

    public List<Product> getLowRatingProducts(List<Product> products, int clusterCount) {
        return getProductsByCriterion(products, clusterCount, "rating", true);
    }

    public List<Product> getHighRatingProducts(List<Product> products, int clusterCount) {
        return getProductsByCriterion(products, clusterCount, "rating", false);
    }

    private List<Product> getProductsByCriterion(List<Product> products, int clusterCount, String criterion, boolean low) {
        List<CentroidCluster<ProductClusterable>> clusters = performClustering(products, clusterCount, criterion);

        List<Product> selectedProducts = new ArrayList<>();
        for (Cluster<ProductClusterable> cluster : clusters) {
            double averageValue = cluster.getPoints().stream().mapToDouble(p -> p.getPoint()[0]).average().orElse(low ? Double.MAX_VALUE : Double.MIN_VALUE);

            if (low && averageValue == clusters.stream()
                    .mapToDouble(c -> c.getPoints().stream()
                    .mapToDouble(p -> p.getPoint()[0])
                    .average()
                    .orElse(Double.MAX_VALUE))
                    .min()
                    .orElse(Double.MAX_VALUE)
                    || !low && averageValue == clusters.stream()
                    .mapToDouble(c -> c.getPoints().stream()
                            .mapToDouble(p -> p.getPoint()[0])
                            .average()
                            .orElse(Double.MIN_VALUE))
                    .max()
                    .orElse(Double.MIN_VALUE)) {
                for (ProductClusterable p : cluster.getPoints()) {
                    selectedProducts.add(p.getProduct());
                }
            }
        }
        return selectedProducts;
    }

    public List<Product> getLowPriceHighRatingProducts(List<Product> products, int clusterCount) {
        List<CentroidCluster<ProductClusterable>> clusters = performClustering(products, clusterCount, "price", "rating");

        List<Product> lowPriceHighRatingProducts = new ArrayList<>();
        for (Cluster<ProductClusterable> cluster : clusters) {
            double averagePrice = cluster.getPoints().stream().mapToDouble(p -> p.getPoint()[0]).average().orElse(Double.MAX_VALUE);
            double averageRating = cluster.getPoints().stream().mapToDouble(p -> p.getPoint()[1]).average().orElse(Double.MIN_VALUE);

            if (averagePrice < 500 && averageRating > 4) {
                for (ProductClusterable p : cluster.getPoints()) {
                    lowPriceHighRatingProducts.add(p.getProduct());
                }
            }
        }
        return lowPriceHighRatingProducts;
    }

    public List<String> getClusterIds(List<Product> products, int clusterCount, String... parameters) {
        List<CentroidCluster<ProductClusterable>> clusters = performClustering(products, clusterCount, parameters);
        List<String> clusterIds = new ArrayList<>();
        for (int i = 0; i < clusters.size(); i++) {
            clusterIds.add(String.valueOf(i));
        }
        return clusterIds;
    }

    private double parsePrice(String price) {
        price = price.replace(",", ".");
        price = price.replaceAll("[^\\d.]", "");
        int firstDotIndex = price.indexOf('.');
        if (firstDotIndex != -1) {
            price = price.substring(0, firstDotIndex + 1) + price.substring(firstDotIndex + 1).replace(".", "");
        }
        return Double.parseDouble(price);
    }
}

class ProductClusterable implements Clusterable {

    private final double[] points;
    private final Product product;

    public ProductClusterable(double[] points, Product product) {
        this.points = points;
        this.product = product;
    }

    @Override
    public double[] getPoint() {
        return points;
    }

    public Product getProduct() {
        return product;
    }
}
