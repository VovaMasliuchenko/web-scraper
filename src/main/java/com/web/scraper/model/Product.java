package com.web.scraper.model;

import jakarta.persistence.*;

import java.util.List;

@Entity
@Table(name = "products")
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "price")
	private String price;
	@ElementCollection
	private List<String> availableColors;
	@Column(name = "name")
	private String name;
	@Column(name = "url")
	private String url;

	public Product() {}

	public Product(String price, List<String> availableColors, String name, String url) {
		this.price = price;
		this.availableColors = availableColors;
		this.name = name;
		this.url = url;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public List<String> getAvailableColors() {
		return availableColors;
	}

	public void setAvailableColors(List<String> availableColors) {
		this.availableColors = availableColors;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}
}