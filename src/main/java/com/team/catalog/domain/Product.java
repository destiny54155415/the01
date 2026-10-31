package com.team.catalog.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 80)
    private String name;

    @Column(nullable = false, length = 40)
    private String brand;

    @Column(nullable = false)
    private int price;

    private Integer salePrice;

    @Column(nullable = false)
    private int sellingPrice;

    @Column(length = 120)
    private String summary;

    @Column(length = 2000)
    private String description;

    @Column(nullable = false)
    private int stock;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProductStatus status = ProductStatus.ON_SALE;

    @Column(nullable = false)
    private int viewCount;

    @Column(nullable = false)
    private int reviewCount;

    @Column(nullable = false)
    private double averageRating;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    protected Product() {
    }

    public Product(Category category, String name, String brand, int price, Integer salePrice, String summary, int stock) {
        this.category = category;
        this.name = name;
        this.brand = brand;
        this.price = price;
        this.salePrice = salePrice;
        this.summary = summary;
        this.stock = stock;
        this.createdAt = LocalDateTime.now();
        syncSellingPrice();
    }

    @PrePersist
    @PreUpdate
    public void syncSellingPrice() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (salePrice != null && salePrice > 0 && salePrice < price) {
            sellingPrice = salePrice;
        } else {
            sellingPrice = price;
        }
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

    public void replaceRating(int reviewCount, double averageRating) {
        this.reviewCount = Math.max(reviewCount, 0);
        this.averageRating = reviewCount <= 0 ? 0 : averageRating;
    }

    public Integer discountPercent() {
        if (salePrice == null || price <= 0 || salePrice >= price) {
            return null;
        }
        return (int) Math.round((price - salePrice) * 100.0 / price);
    }

    public Long getId() {
        return id;
    }

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getBrand() {
        return brand;
    }

    public void setBrand(String brand) {
        this.brand = brand;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public Integer getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(Integer salePrice) {
        this.salePrice = salePrice;
    }

    public int getSellingPrice() {
        return sellingPrice;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }

    public int getViewCount() {
        return viewCount;
    }

    public int getReviewCount() {
        return reviewCount;
    }

    public double getAverageRating() {
        return averageRating;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
