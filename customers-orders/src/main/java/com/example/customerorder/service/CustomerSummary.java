package com.example.customerorder.service;

import java.time.LocalDateTime;

public record CustomerSummary(
        Long id,
        String name,
        String email,
        String phone,
        LocalDateTime createdAt,
        long orderCount
) {
}
