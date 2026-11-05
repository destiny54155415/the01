package com.example.shopping_project.service.productservice;

import com.example.shopping_project.dto.productdto.ProductDto;
import com.example.shopping_project.entity.productentity.CategoryEntity;
import com.example.shopping_project.entity.productentity.PetType;
import com.example.shopping_project.entity.productentity.ProductEntity;
import com.example.shopping_project.repository.productrepository.ProductRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class ProductService {

    public static final int PAGE_SIZE = 12;

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    public ProductService(ProductRepository productRepository, CategoryService categoryService) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
    }

    @Transactional(readOnly = true)
    public Page<ProductEntity> searchActive(String keyword, Long categoryId, PetType petType,
                                            BigDecimal minPrice, BigDecimal maxPrice, int page) {
        return productRepository.search(
                blankToNull(keyword),
                categoryId,
                petType,
                minPrice,
                maxPrice,
                false,
                PageRequest.of(Math.max(page, 0), PAGE_SIZE,
                        Sort.by(Sort.Direction.DESC, "salesCount", "productId"))
        );
    }

    @Transactional(readOnly = true)
    public List<ProductEntity> findBestsellers(int limit) {
        int size = Math.max(limit, 1);
        return productRepository
                .findByIsActiveTrueOrderBySalesCountDescProductIdDesc(PageRequest.of(0, size))
                .getContent();
    }

    @Transactional(readOnly = true)
    public Page<ProductEntity> searchAdmin(String keyword, Long categoryId, PetType petType, int page) {
        return productRepository.search(
                blankToNull(keyword),
                categoryId,
                petType,
                null,
                null,
                true,
                PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by(Sort.Direction.DESC, "updatedAt", "productId"))
        );
    }

    @Transactional(readOnly = true)
    public ProductEntity getActiveProduct(Long productId) {
        ProductEntity product = productRepository.findWithCategoryByProductId(productId)
                .orElseThrow(() -> new NotFoundException("상품을 찾을 수 없습니다."));
        if (!Boolean.TRUE.equals(product.getIsActive())
                || !Boolean.TRUE.equals(product.getCategory().getIsActive())) {
            throw new NotFoundException("상품을 찾을 수 없습니다.");
        }
        return product;
    }

    @Transactional(readOnly = true)
    public ProductEntity getProduct(Long productId) {
        return productRepository.findWithCategoryByProductId(productId)
                .orElseThrow(() -> new NotFoundException("상품을 찾을 수 없습니다."));
    }

    public ProductEntity create(ProductDto form) {
        CategoryEntity category = categoryService.getCategory(form.getCategoryId());
        ProductEntity product = new ProductEntity(
                category,
                form.getProductName().trim(),
                blankToNull(form.getDescription()),
                form.getPrice(),
                form.getStock(),
                form.getSalesCount() == null ? 0 : form.getSalesCount(),
                form.getPetType(),
                blankToNull(form.getImageUrl())
        );
        product.setImageUrl2(blankToNull(form.getImageUrl2()));
        product.setImageUrl3(blankToNull(form.getImageUrl3()));
        product.setIsActive(form.getIsActive() == null || form.getIsActive());
        return productRepository.save(product);
    }

    public ProductEntity update(Long productId, ProductDto form) {
        ProductEntity product = getProduct(productId);
        CategoryEntity category = categoryService.getCategory(form.getCategoryId());
        product.setCategory(category);
        product.setProductName(form.getProductName().trim());
        product.setDescription(blankToNull(form.getDescription()));
        product.setPrice(form.getPrice());
        product.setStock(form.getStock());
        product.setSalesCount(form.getSalesCount() == null ? 0 : form.getSalesCount());
        product.setPetType(form.getPetType());
        product.setImageUrl(blankToNull(form.getImageUrl()));
        product.setImageUrl2(blankToNull(form.getImageUrl2()));
        product.setImageUrl3(blankToNull(form.getImageUrl3()));
        product.setIsActive(form.getIsActive() == null || form.getIsActive());
        return product;
    }

    public void deactivate(Long productId) {
        getProduct(productId).deactivate();
    }

    public void delete(Long productId) {
        productRepository.delete(getProduct(productId));
    }

    private String blankToNull(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
