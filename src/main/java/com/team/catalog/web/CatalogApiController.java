package com.team.catalog.web;

import com.team.catalog.domain.Category;
import com.team.catalog.domain.Product;
import com.team.catalog.domain.ProductSort;
import com.team.catalog.service.CategoryService;
import com.team.catalog.service.ProductService;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class CatalogApiController {

    private final ProductService productService;
    private final CategoryService categoryService;

    public CatalogApiController(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }

    @GetMapping("/api/categories")
    public List<CategoryResponse> categories() {
        return categoryService.tree().stream().map(CategoryResponse::from).toList();
    }

    @GetMapping("/api/products")
    public PageResponse products(@RequestParam(required = false) String q,
                                 @RequestParam(required = false) Long categoryId,
                                 @RequestParam(defaultValue = "NEWEST") String sort,
                                 @RequestParam(defaultValue = "0") int page) {
        Page<Product> result = productService.search(q, categoryId, ProductSort.from(sort), page, false);
        return PageResponse.from(result);
    }

    @GetMapping("/api/products/{id}")
    public ProductResponse product(@PathVariable Long id) {
        return ProductResponse.from(productService.get(id));
    }

    public record CategoryResponse(Long id, String name, int sortOrder, boolean visible, List<CategoryResponse> children) {
        static CategoryResponse from(Category category) {
            return new CategoryResponse(
                    category.getId(),
                    category.getName(),
                    category.getSortOrder(),
                    category.isVisible(),
                    category.getChildren().stream().filter(Category::isVisible).map(CategoryResponse::from).toList()
            );
        }
    }

    public record ProductResponse(Long id, String name, String brand, Long categoryId, String categoryName,
                                  int price, Integer salePrice, int sellingPrice, Integer discountPercent,
                                  int stock, String status, int reviewCount, double averageRating) {
        static ProductResponse from(Product product) {
            return new ProductResponse(
                    product.getId(),
                    product.getName(),
                    product.getBrand(),
                    product.getCategory().getId(),
                    product.getCategory().getName(),
                    product.getPrice(),
                    product.getSalePrice(),
                    product.getSellingPrice(),
                    product.discountPercent(),
                    product.getStock(),
                    product.getStatus().name(),
                    product.getReviewCount(),
                    product.getAverageRating()
            );
        }
    }

    public record PageResponse(List<ProductResponse> items, int page, int totalPages, long totalCount) {
        static PageResponse from(Page<Product> page) {
            return new PageResponse(
                    page.getContent().stream().map(ProductResponse::from).toList(),
                    page.getNumber(),
                    page.getTotalPages(),
                    page.getTotalElements()
            );
        }
    }
}
