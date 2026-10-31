package com.example.ecommerce.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name="PRODUCTS")
public class Product {

    @Id
    @SequenceGenerator(name="productSeq", sequenceName="PRODUCTS_SEQ", allocationSize=1)
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="productSeq")
    @Column(name="PRODUCT_ID", nullable=false)
    private Long productId;

    @Column(name="CATEGORY_ID", nullable=true)
    private Long categoryId;

    @Column(name="PRODUCT_NAME", nullable=true, length=150)
    private String productName;

    @Column(name="DESCRIPTION", nullable=false, length=1000)
    private String description;

    @Column(name="BRAND", nullable=false, length=100)
    private String brand;

    @Column(name="PRICE", nullable=false, precision=10, scale=2)
    private BigDecimal price;

    @Column(name="STOCK_QUANTITY", nullable=true)
    private Integer stockQuantity;

    @Column(name="IMAGE_URL", nullable=false, length=500)
    private String imageUrl;

    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name="IS_ACTIVE", nullable=true, length=1)
    private String isActive;

    @Column(name="CREATED_AT", nullable=true, insertable=false, updatable=false)
    private LocalDateTime createdAt;

    public Product() {}

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getStockQuantity() { return stockQuantity; }
    public void setStockQuantity(Integer stockQuantity) { this.stockQuantity = stockQuantity; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getIsActive() { return isActive; }
    public void setIsActive(String isActive) { this.isActive = isActive; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

}