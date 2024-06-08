package com.web.scraper.repository;

import java.util.List;
import java.util.Optional;

import com.web.scraper.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ProductRepository extends JpaRepository<Product, Long> {
	Optional<Product> findByName(String name);
	List<Product> findByNameContaining(String name);
	List<Product> findByKeyword(String keyword);
}
