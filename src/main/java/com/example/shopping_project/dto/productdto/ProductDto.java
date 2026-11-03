package com.example.shopping_project.dto.productdto;

import com.example.shopping_project.entity.productentity.PetType;
import com.example.shopping_project.entity.productentity.ProductEntity;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;

public class ProductDto {

    private Long productId;

    @NotNull(message = "카테고리를 선택해 주세요.")
    private Long categoryId;

    @NotBlank(message = "상품명을 입력해 주세요.")
    @Size(max = 150, message = "상품명은 150자 이하입니다.")
    private String productName;

    @Size(max = 4000, message = "상품 설명이 너무 깁니다.")
    private String description;

    @NotNull(message = "가격을 입력해 주세요.")
    @DecimalMin(value = "0.0", inclusive = true, message = "가격은 0원 이상입니다.")
    private BigDecimal price;

    @NotNull(message = "재고를 입력해 주세요.")
    @Min(value = 0, message = "재고는 0개 이상입니다.")
    private Integer stock;

    @NotNull(message = "판매량을 입력해 주세요.")
    @Min(value = 0, message = "판매량은 0 이상입니다.")
    private Integer salesCount = 0;

    @NotNull(message = "반려동물 구분을 선택해 주세요.")
    private PetType petType = PetType.COMMON;

    @Size(max = 500, message = "이미지 주소는 500자 이하입니다.")
    private String imageUrl;

    private Boolean isActive = true;

    public static ProductDto from(ProductEntity product) {
        ProductDto form = new ProductDto();
        form.productId = product.getProductId();
        form.categoryId = product.getCategory().getCategoryId();
        form.productName = product.getProductName();
        form.description = product.getDescription();
        form.price = product.getPrice();
        form.stock = product.getStock();
        form.salesCount = product.getSalesCount();
        form.petType = product.getPetType();
        form.imageUrl = product.getImageUrl();
        form.isActive = product.getIsActive();
        return form;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
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

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean active) {
        isActive = active;
    }
}
