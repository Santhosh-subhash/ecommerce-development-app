package com.example.ecommerce.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name="ORDER_ITEMS")
public class OrderItem {

    @Id
    @SequenceGenerator(name="orderitemSeq", sequenceName="ORDER_ITEMS_SEQ", allocationSize=1)
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="orderitemSeq")
    @Column(name="ORDER_ITEM_ID", nullable=false)
    private Long orderItemId;

    @Column(name="ORDER_ID", nullable=true)
    private Long orderId;

    @Column(name="PRODUCT_ID", nullable=true)
    private Long productId;

    @Column(name="QUANTITY", nullable=true)
    private Integer quantity;

    @Column(name="UNIT_PRICE", nullable=false, precision=10, scale=2)
    private BigDecimal unitPrice;

    @Column(name="LINE_TOTAL", nullable=false, precision=12, scale=2)
    private BigDecimal lineTotal;

    public OrderItem() {}

    public Long getOrderItemId() { return orderItemId; }
    public void setOrderItemId(Long orderItemId) { this.orderItemId = orderItemId; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public BigDecimal getUnitPrice() { return unitPrice; }
    public void setUnitPrice(BigDecimal unitPrice) { this.unitPrice = unitPrice; }

    public BigDecimal getLineTotal() { return lineTotal; }
    public void setLineTotal(BigDecimal lineTotal) { this.lineTotal = lineTotal; }

}