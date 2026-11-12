package com.example.shopping_project.dto.review;

import java.time.LocalDateTime;

public class ReviewResponseDTO {

    private Long reviewId;
    private String memberName;
    private String productName;
    private Integer rating;
    private String content;
    private LocalDateTime createdAt;

    public ReviewResponseDTO(Long reviewId, String memberName,
                             String productName, Integer rating,
                             String content, LocalDateTime createdAt) {
        this.reviewId = reviewId;
        this.memberName = memberName;
        this.productName = productName;
        this.rating = rating;
        this.content = content;
        this.createdAt = createdAt;
    }

    public Long getReviewId() {
        return reviewId;
    }

    public String getMemberName() {
        return memberName;
    }

    public String getProductName() {
        return productName;
    }

    public Integer getRating() {
        return rating;
    }

    public String getContent() {
        return content;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}