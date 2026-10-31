package com.team.catalog.web;

import com.team.catalog.domain.Product;
import com.team.catalog.domain.ProductStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ProductForm {

    private Long id;

    @NotNull(message = "카테고리를 선택해 주세요.")
    private Long categoryId;

    @NotBlank(message = "상품명을 입력해 주세요.")
    @Size(max = 80, message = "상품명은 80자 이하입니다.")
    private String name;

    @NotBlank(message = "브랜드를 입력해 주세요.")
    @Size(max = 40, message = "브랜드는 40자 이하입니다.")
    private String brand;

    @NotNull(message = "가격을 입력해 주세요.")
    @Min(value = 0, message = "가격은 0원 이상입니다.")
    private Integer price;

    @Min(value = 0, message = "할인가격은 0원 이상입니다.")
    private Integer salePrice;

    @Size(max = 120, message = "한줄 설명은 120자 이하입니다.")
    private String summary;

    @Size(max = 2000, message = "상세 설명은 2000자 이하입니다.")
    private String description;

    @NotNull(message = "재고를 입력해 주세요.")
    @Min(value = 0, message = "재고는 0개 이상입니다.")
    private Integer stock;

    @NotNull(message = "판매 상태를 선택해 주세요.")
    private ProductStatus status = ProductStatus.ON_SALE;

    public static ProductForm from(Product product) {
        ProductForm form = new ProductForm();
        form.id = product.getId();
        form.categoryId = product.getCategory().getId();
        form.name = product.getName();
        form.brand = product.getBrand();
        form.price = product.getPrice();
        form.salePrice = product.getSalePrice();
        form.summary = product.getSummary();
        form.description = product.getDescription();
        form.stock = product.getStock();
        form.status = product.getStatus();
        return form;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
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

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public Integer getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(Integer salePrice) {
        this.salePrice = salePrice;
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

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public ProductStatus getStatus() {
        return status;
    }

    public void setStatus(ProductStatus status) {
        this.status = status;
    }
}
