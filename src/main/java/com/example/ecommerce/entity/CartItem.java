package com.example.ecommerce.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name="CART_ITEMS")
public class CartItem {

    @Id
    @SequenceGenerator(name="cartitemSeq", sequenceName="CART_ITEMS_SEQ", allocationSize=1)
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="cartitemSeq")
    @Column(name="CART_ITEM_ID", nullable=false)
    private Long cartItemId;

    @Column(name="CART_ID", nullable=true)
    private Long cartId;

    @Column(name="PRODUCT_ID", nullable=true)
    private Long productId;

    @Column(name="QUANTITY", nullable=true)
    private Integer quantity;

    @Column(name="UNIT_PRICE", nullable=false, precision=10, scale=2)
    private BigDecimal unitPrice;

    @Column(name="ADDED_AT", nullable=true, insertable=false, updatable=false)
    private LocalDateTime addedAt;

    public CartItem() {}

    public Long getCartItemId() { return cartItemId; }
    public void setCartItemId(Long cartItemId) { this.cartItemId = cartItemId; }

    public Long getCartId() { return cartId; }
    public void setCartId(Long cartId) { this.cartId = cartId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public LocalDateTime getAddedAt() { return addedAt; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }

}