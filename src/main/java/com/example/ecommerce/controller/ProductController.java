package com.example.ecommerce.controller;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.service.ProductService;

@RestController
@RequestMapping("/api/products")
public class ProductController {
    private final ProductService service;
    public ProductController(ProductService service) { this.service = service; }

    @GetMapping
    public List<Product> list(@RequestParam(required = false) String q,
                              @RequestParam(required = false) Long categoryId,
                              @RequestParam(required = false) String brand,
                              @RequestParam(required = false) BigDecimal minPrice,
                              @RequestParam(required = false) BigDecimal maxPrice,
                              @RequestParam(required = false, defaultValue = "name-asc") String sort) {
        return service.search(q, categoryId, brand, minPrice, maxPrice, sort);
    }
    @GetMapping("/{id}") public Product get(@PathVariable Long id) { return service.get(id); }
}
