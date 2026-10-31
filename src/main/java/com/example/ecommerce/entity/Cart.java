package com.example.ecommerce.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name="CARTS")
public class Cart {

    @Id
    @SequenceGenerator(name="cartSeq", sequenceName="CARTS_SEQ", allocationSize=1)
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="cartSeq")
    @Column(name="CART_ID", nullable=false)
    private Long cartId;

    @Column(name="USER_ID", nullable=true)
    private Long userId;

    @Column(name="STATUS", nullable=true, length=20)
    private String status;

    @Column(name="CREATED_AT", nullable=true, insertable=false, updatable=false)
    private LocalDateTime createdAt;

    @Column(name="UPDATED_AT", nullable=true, insertable=false, updatable=false)
    private LocalDateTime updatedAt;

    public Cart() {}

    public Long getCartId() { return cartId; }
    public void setCartId(Long cartId) { this.cartId = cartId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

}