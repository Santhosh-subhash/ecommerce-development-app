package com.example.ecommerce.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="CATEGORIES")
public class Category {

    @Id
    @SequenceGenerator(name="categorySeq", sequenceName="CATEGORIES_SEQ", allocationSize=1)
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="categorySeq")
    @Column(name="CATEGORY_ID", nullable=false)
    private Long categoryId;

    @Column(name="CATEGORY_NAME", nullable=true, length=100)
    private String categoryName;

    @Column(name="DESCRIPTION", nullable=false, length=500)
    private String description;

    @Column(name="CREATED_AT", nullable=true, insertable=false, updatable=false)
    private LocalDateTime createdAt;

    public Category() {}

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getCategoryName() { return categoryName; }
    public void setCategoryName(String categoryName) { this.categoryName = categoryName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

}