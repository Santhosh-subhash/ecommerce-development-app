package com.example.ecommerce.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name="ORDERS")
public class Order {

    @Id
    @SequenceGenerator(name="orderSeq", sequenceName="ORDERS_SEQ", allocationSize=1)
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="orderSeq")
    @Column(name="ORDER_ID", nullable=false)
    private Long orderId;

    @Column(name="USER_ID", nullable=true)
    private Long userId;

    @Column(name="ADDRESS_ID", nullable=true)
    private Long addressId;

    @Column(name="ORDER_DATE", nullable=true, insertable=false, updatable=false)
    private LocalDateTime orderDate;

    @Column(name="STATUS", nullable=true, length=20)
    private String status;

    @Column(name="SUBTOTAL", nullable=false, precision=12, scale=2)
    private BigDecimal subtotal;

    @Column(name="SHIPPING_FEE", nullable=false, precision=10, scale=2)
    private BigDecimal shippingFee;

    @Column(name="TAX_AMOUNT", nullable=false, precision=10, scale=2)
    private BigDecimal taxAmount;

    @Column(name="TOTAL_AMOUNT", nullable=false, precision=12, scale=2)
    private BigDecimal totalAmount;

    public Order() {}

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public Long getAddressId() { return addressId; }
    public void setAddressId(Long addressId) { this.addressId = addressId; }

    public LocalDateTime getOrderDate() { return orderDate; }
    public void setOrderDate(LocalDateTime orderDate) { this.orderDate = orderDate; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public BigDecimal getSubtotal() { return subtotal; }
    public void setSubtotal(BigDecimal subtotal) { this.subtotal = subtotal; }

    public BigDecimal getShippingFee() { return shippingFee; }
    public void setShippingFee(BigDecimal shippingFee) { this.shippingFee = shippingFee; }

    public BigDecimal getTaxAmount() { return taxAmount; }
    public void setTaxAmount(BigDecimal taxAmount) { this.taxAmount = taxAmount; }

    public BigDecimal getTotalAmount() { return totalAmount; }
    public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }

}