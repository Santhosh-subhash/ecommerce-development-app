package com.example.ecommerce.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.math.BigDecimal;

@Entity
@Table(name="PAYMENTS")
public class Payment {

    @Id
    @SequenceGenerator(name="paymentSeq", sequenceName="PAYMENTS_SEQ", allocationSize=1)
    @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="paymentSeq")
    @Column(name="PAYMENT_ID", nullable=false)
    private Long paymentId;

    @Column(name="ORDER_ID", nullable=true)
    private Long orderId;

    @Column(name="PAYMENT_METHOD", nullable=true, length=20)
    private String paymentMethod;

    @Column(name="PAYMENT_STATUS", nullable=true, length=20)
    private String paymentStatus;

    @Column(name="AMOUNT", nullable=false, precision=12, scale=2)
    private BigDecimal amount;

    @Column(name="TRANSACTION_REF", nullable=false, length=100)
    private String transactionRef;

    @Column(name="PAID_AT", nullable=false, insertable=false, updatable=false)
    private LocalDateTime paidAt;

    @Column(name="CREATED_AT", nullable=true, insertable=false, updatable=false)
    private LocalDateTime createdAt;

    public Payment() {}

    public Long getPaymentId() { return paymentId; }
    public void setPaymentId(Long paymentId) { this.paymentId = paymentId; }

    public Long getOrderId() { return orderId; }
    public void setOrderId(Long orderId) { this.orderId = orderId; }

    public String getPaymentMethod() { return paymentMethod; }
    public void setPaymentMethod(String paymentMethod) { this.paymentMethod = paymentMethod; }

    public String getPaymentStatus() { return paymentStatus; }
    public void setPaymentStatus(String paymentStatus) { this.paymentStatus = paymentStatus; }

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public String getTransactionRef() { return transactionRef; }
    public void setTransactionRef(String transactionRef) { this.transactionRef = transactionRef; }

    public LocalDateTime getPaidAt() { return paidAt; }
    public void setPaidAt(LocalDateTime paidAt) { this.paidAt = paidAt; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

}