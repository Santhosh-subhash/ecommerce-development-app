package com.example.ecommerce.controller;

import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.ecommerce.entity.Category;
import com.example.ecommerce.repository.CategoryRepository;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {
    private final CategoryRepository repository;
    public CategoryController(CategoryRepository repository) { this.repository = repository; }
    @GetMapping public List<Category> list() { return repository.findAll(); }
}
