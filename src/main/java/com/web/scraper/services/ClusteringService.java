package com.web.scraper.services;

import com.web.scraper.model.Product;
import org.apache.commons.math3.ml.clustering.CentroidCluster;
import org.apache.commons.math3.ml.clustering.Cluster;
import org.apache.commons.math3.ml.clustering.Clusterable;
import org.apache.commons.math3.ml.clustering.KMeansPlusPlusClusterer;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
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

    public void visualizeClustering(List<Product> products, int clusterCount, String outputFile, String... parameters) throws IOException {
        List<CentroidCluster<ProductClusterable>> clusters = performClustering(products, clusterCount, parameters);

        XYSeriesCollection dataset = new XYSeriesCollection();
        int clusterIndex = 0;
        for (CentroidCluster<ProductClusterable> cluster : clusters) {
            XYSeries series = new XYSeries("Cluster " + clusterIndex++);
            for (ProductClusterable point : cluster.getPoints()) {
                series.add(point.getPoint()[0], point.getPoint()[1]);
            }
            dataset.addSeries(series);
        }

        JFreeChart scatterPlot = ChartFactory.createScatterPlot(
                "Product Clustering",
                parameters[0],
                parameters[1],
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        XYPlot plot = (XYPlot) scatterPlot.getPlot();
        XYItemRenderer renderer = new XYLineAndShapeRenderer(false, true);
        plot.setRenderer(renderer);

        NumberAxis domainAxis = (NumberAxis) plot.getDomainAxis();
        domainAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        domainAxis.setTickUnit(new NumberTickUnit(1));

        ChartUtilities.saveChartAsPNG(new File(outputFile), scatterPlot, 800, 600);
    }

    public void visualizeElbowMethod(List<Product> products, int maxClusters, String outputFile, String... parameters) throws IOException {
        XYSeries series = new XYSeries("Elbow Method");

        for (int k = 1; k <= maxClusters; k++) {
            double distortion = calculateDistortion(products, k, parameters);
            series.add(k, distortion);
        }

        XYSeriesCollection dataset = new XYSeriesCollection(series);
        JFreeChart chart = ChartFactory.createXYLineChart(
                "Elbow Method for Optimal K",
                "Number of Clusters",
                "Distortion",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );

        XYPlot plot = (XYPlot) chart.getPlot();
        XYItemRenderer renderer = new XYLineAndShapeRenderer(true, false);
        plot.setRenderer(renderer);

        ChartUtilities.saveChartAsPNG(new File(outputFile), chart, 800, 600);
    }

    private double calculateDistortion(List<Product> products, int k, String... parameters) {
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

        KMeansPlusPlusClusterer<ProductClusterable> clusterer = new KMeansPlusPlusClusterer<>(k);
        List<CentroidCluster<ProductClusterable>> clusters = clusterer.cluster(points);

        double distortion = 0.0;
        for (CentroidCluster<ProductClusterable> cluster : clusters) {
            double[] centroid = cluster.getCenter().getPoint();
            for (ProductClusterable point : cluster.getPoints()) {
                distortion += calculateDistance(point.getPoint(), centroid);
            }
        }
        return distortion;
    }

    private double calculateDistance(double[] point, double[] centroid) {
        double sum = 0.0;
        for (int i = 0; i < point.length; i++) {
            double diff = point[i] - centroid[i];
            sum += diff * diff;
        }
        return Math.sqrt(sum);
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
