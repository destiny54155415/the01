package com.example.shopping_project.config.productconfig;

import com.example.shopping_project.service.productservice.ProductImageStorageService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class ProductUploadWebConfig implements WebMvcConfigurer {

    private final ProductImageStorageService productImageStorageService;

    public ProductUploadWebConfig(ProductImageStorageService productImageStorageService) {
        this.productImageStorageService = productImageStorageService;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String location = productImageStorageService.getUploadRoot().toUri().toString();
        if (!location.endsWith("/")) {
            location = location + "/";
        }
        registry.addResourceHandler("/uploads/products/**")
                .addResourceLocations(location);
    }
}
