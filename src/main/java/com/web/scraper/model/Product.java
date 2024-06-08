package com.web.scraper.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "products")
public class Product {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@Column(name = "price")
	private String price;
	@Column(name = "name")
	private String name;
	@Column(name = "url")
	private String url;
	@Column(name = "keyword")
	private String keyword;
	@ManyToMany
	@JoinTable(
			name = "product_color",
			joinColumns = @JoinColumn(name = "product_id"),
			inverseJoinColumns = @JoinColumn(name = "color_id")
	)
	@JsonManagedReference
	private Set<AvailableColor> availableColors = new HashSet<>();

	public Product() {}

	public Product(String price, String name, String url, String keyword) {
		this.price = price;
		this.name = name;
		this.url = url;
		this.keyword = keyword;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPrice() {
		return price;
	}

	public void setPrice(String price) {
		this.price = price;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public Set<AvailableColor> getAvailableColors() {
		return availableColors;
	}

	public void setAvailableColors(Set<AvailableColor> availableColors) {
		this.availableColors = availableColors;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
}