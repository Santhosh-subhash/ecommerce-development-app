package com.example.ecommerce.service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.ecommerce.entity.Product;
import com.example.ecommerce.repository.ProductRepository;

@Service
public class ProductService {
    private final ProductRepository repository;
    public ProductService(ProductRepository repository) { this.repository = repository; }

    public List<Product> search(String q, Long categoryId, String brand, BigDecimal minPrice,
                                BigDecimal maxPrice, String sort) {
        String query = q == null ? "" : q.trim();
        if (query.length() > 100) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Search text must not exceed 100 characters");
        if (minPrice != null && minPrice.signum() < 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Minimum price cannot be negative");
        if (maxPrice != null && maxPrice.signum() < 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Maximum price cannot be negative");
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Minimum price cannot exceed maximum price");
        String needle = query.toLowerCase(Locale.ROOT);
        String brandNeedle = brand == null ? "" : brand.trim().toLowerCase(Locale.ROOT);
        List<Product> products = repository.findAll().stream()
            .filter(p -> p.getIsActive() == null || "Y".equalsIgnoreCase(p.getIsActive()))
            .filter(p -> needle.isEmpty() || contains(p.getProductName(), needle) || contains(p.getDescription(), needle) || contains(p.getBrand(), needle) || (p.getProductId() != null && p.getProductId().toString().contains(needle)))
            .filter(p -> categoryId == null || categoryId.equals(p.getCategoryId()))
            .filter(p -> brandNeedle.isEmpty() || contains(p.getBrand(), brandNeedle))
            .filter(p -> minPrice == null || p.getPrice().compareTo(minPrice) >= 0)
            .filter(p -> maxPrice == null || p.getPrice().compareTo(maxPrice) <= 0)
            .toList();
        Comparator<Product> comparator = Comparator.comparing(Product::getProductName, Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        if ("price-asc".equalsIgnoreCase(sort)) comparator = Comparator.comparing(Product::getPrice, Comparator.nullsLast(BigDecimal::compareTo));
        else if ("price-desc".equalsIgnoreCase(sort)) comparator = Comparator.comparing(Product::getPrice, Comparator.nullsLast(BigDecimal::compareTo)).reversed();
        else if ("name-desc".equalsIgnoreCase(sort)) comparator = comparator.reversed();
        return products.stream().sorted(comparator).toList();
    }
    public List<Product> search(String q) { return search(q, null, null, null, null, "name-asc"); }
    public Product get(Long id) {
        Product product = repository.findById(id).orElseThrow(() -> new ProductNotFoundException(id));
        if (product.getIsActive() != null && !"Y".equalsIgnoreCase(product.getIsActive())) throw new ProductNotFoundException(id);
        return product;
    }
    private boolean contains(String value, String needle) { return value != null && value.toLowerCase(Locale.ROOT).contains(needle); }
}
