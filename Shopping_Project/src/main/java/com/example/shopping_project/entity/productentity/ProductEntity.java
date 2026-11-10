package com.example.shopping_project.entity.productentity;

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
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "products")
public class ProductEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private CategoryEntity category;

    @Column(name = "product_name", nullable = false, length = 150)
    private String productName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(name = "stock", nullable = false)
    private Integer stock = 0;

    /** 누적 판매량 — 인기/추천 정렬에 사용 */
    @Column(name = "sales_count", nullable = false)
    private Integer salesCount = 0;

    @Enumerated(EnumType.STRING)
    @Column(name = "pet_type", nullable = false, length = 20)
    private PetType petType = PetType.COMMON;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    /** 상세 갤러리: 박스 형태 */
    @Column(name = "image_url_2", length = 500)
    private String imageUrl2;

    /** 상세 갤러리: 제품 분리/구성컷 */
    @Column(name = "image_url_3", length = 500)
    private String imageUrl3;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected ProductEntity() {
    }

    public ProductEntity(CategoryEntity category, String productName, String description, BigDecimal price,
                   Integer stock, PetType petType, String imageUrl) {
        this(category, productName, description, price, stock, 0, petType, imageUrl);
    }

    public ProductEntity(CategoryEntity category, String productName, String description, BigDecimal price,
                   Integer stock, Integer salesCount, PetType petType, String imageUrl) {
        this.category = category;
        this.productName = productName;
        this.description = description;
        this.price = price;
        this.stock = stock;
        this.salesCount = salesCount == null ? 0 : salesCount;
        this.petType = petType;
        this.imageUrl = imageUrl;
        this.isActive = true;
    }

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        if (stock == null) {
            stock = 0;
        }
        if (salesCount == null) {
            salesCount = 0;
        }
        if (petType == null) {
            petType = PetType.COMMON;
        }
        if (isActive == null) {
            isActive = true;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void deactivate() {
        this.isActive = false;
    }

    public Long getProductId() {
        return productId;
    }

    public CategoryEntity getCategory() {
        return category;
    }

    public void setCategory(CategoryEntity category) {
        this.category = category;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public Integer getSalesCount() {
        return salesCount;
    }

    public void setSalesCount(Integer salesCount) {
        this.salesCount = salesCount;
    }

    public PetType getPetType() {
        return petType;
    }

    public void setPetType(PetType petType) {
        this.petType = petType;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl2() {
        return imageUrl2;
    }

    public void setImageUrl2(String imageUrl2) {
        this.imageUrl2 = imageUrl2;
    }

    public String getImageUrl3() {
        return imageUrl3;
    }

    public void setImageUrl3(String imageUrl3) {
        this.imageUrl3 = imageUrl3;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
