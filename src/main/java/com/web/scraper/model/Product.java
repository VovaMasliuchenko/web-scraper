package com.web.scraper.model;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
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
	@Column(name = "rating")
	private int rating;
	@Column(name = "url")
	private String url;
	@Column(name = "keyword")
	private String keyword;
	@Column(name = "last_updated")
	@Temporal(TemporalType.TIMESTAMP)
	private Date lastUpdated;
	@ManyToMany
	@JoinTable(
			name = "product_color",
			joinColumns = @JoinColumn(name = "product_id"),
			inverseJoinColumns = @JoinColumn(name = "color_id")
	)
	@JsonManagedReference
	private Set<AvailableColor> availableColors = new HashSet<>();

	public Product() {
		this.lastUpdated = Calendar.getInstance().getTime();
	}

	public Product(String price, String name, int rating, String url, String keyword) {
		this.price = price;
		this.name = name;
		this.rating = rating;
		this.url = url;
		this.keyword = keyword;
		this.lastUpdated = Calendar.getInstance().getTime();
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

	public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
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

	public Date getLastUpdated() {
		return lastUpdated;
	}

	public void setLastUpdated(Date lastUpdated) {
		this.lastUpdated = lastUpdated;
	}
}