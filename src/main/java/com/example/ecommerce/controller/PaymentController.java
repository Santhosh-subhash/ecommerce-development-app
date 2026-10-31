package com.example.ecommerce.controller;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.Payment;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.PaymentRepository;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {
    private final PaymentRepository paymentRepository;
    private final OrderRepository orderRepository;
    public PaymentController(PaymentRepository paymentRepository, OrderRepository orderRepository) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
    }

    private Long userId(HttpSession session) {
        Object id = session.getAttribute("userId");
        if (id == null) throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in before payment");
        return Long.valueOf(id.toString());
    }

    @PostMapping("/mock/validate")
    public ResponseEntity<Map<String, Object>> validateMockPayment(@RequestBody Map<String, String> body, HttpSession session) {
        userId(session);
        String method = value(body, "paymentMethod").toUpperCase(Locale.ROOT);
        if (method.isBlank()) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Please select a payment method");
        if (method.equals("CREDIT_CARD") || method.equals("DEBIT_CARD")) {
            String card = value(body, "cardNumber").replaceAll("\\s", "");
            String expiry = value(body, "expiry");
            String cvv = value(body, "cvv");
            if (!card.matches("[0-9]{13,19}")) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Card number must contain 13 to 19 digits");
            if (!expiry.matches("(0[1-9]|1[0-2])/[0-9]{2}")) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Expiry must use MM/YY format");
            if (!cvv.matches("[0-9]{3,4}")) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "CVV must contain 3 or 4 digits");
            // Test-only deterministic decline path. No real card data is stored.
            if (card.endsWith("0000")) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Mock payment declined for testing");
            String[] parts = expiry.split("/");
            java.time.YearMonth month = java.time.YearMonth.of(2000 + Integer.parseInt(parts[1]), Integer.parseInt(parts[0]));
            if (month.isBefore(java.time.YearMonth.now())) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Card has expired");
        } else if (!method.equals("UPI") && !method.equals("NET_BANKING") && !method.equals("COD")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported payment method");
        }
        return ResponseEntity.ok(Map.of("valid", true, "message", "Mock payment validation successful"));
    }

    @PostMapping("/mock")
    public ResponseEntity<Map<String, Object>> recordMockPayment(@RequestBody Map<String, Object> body, HttpSession session) {
        Long user = userId(session);
        Object orderValue = body.get("orderId");
        if (orderValue == null) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "orderId is required");
        Long orderId;
        try { orderId = Long.valueOf(orderValue.toString()); }
        catch (NumberFormatException ex) { throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid orderId"); }
        Order order = orderRepository.findById(orderId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Order not found"));
        if (!user.equals(order.getUserId())) throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot pay for another user's order");
        String method = body.getOrDefault("paymentMethod", "").toString().toUpperCase(Locale.ROOT);
        if (!method.matches("UPI|CREDIT_CARD|DEBIT_CARD|NET_BANKING|COD")) throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Unsupported payment method");
        Payment payment = new Payment();
        payment.setOrderId(orderId);
        payment.setPaymentMethod(method);
        payment.setPaymentStatus("COD".equals(method) ? "PENDING" : "SUCCESS");
        payment.setAmount(order.getTotalAmount() == null ? BigDecimal.ZERO : order.getTotalAmount());
        payment.setTransactionRef("MOCK-" + UUID.randomUUID());
        paymentRepository.save(payment);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("paymentStatus", payment.getPaymentStatus(), "transactionRef", payment.getTransactionRef(), "message", "Mock payment recorded"));
    }
    private String value(Map<String, String> body, String key) { String v = body == null ? null : body.get(key); return v == null ? "" : v.trim(); }
}
