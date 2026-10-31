package com.example.ecommerce.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.ecommerce.entity.Payment;

public interface PaymentRepository extends JpaRepository<Payment, Long> {
}
