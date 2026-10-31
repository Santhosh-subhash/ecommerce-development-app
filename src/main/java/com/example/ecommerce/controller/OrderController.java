package com.example.ecommerce.controller;

import java.util.List;
import java.util.Map;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.example.ecommerce.entity.Order;
import com.example.ecommerce.service.OrderService;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    private Long getLoggedInUserId(HttpSession session) {
        Object value = session.getAttribute("userId");

        if (value == null) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Please log in before placing an order");
        }

        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException ex) {
            session.invalidate();

            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid session. Please log in again");
        }
    }

    // POST /api/orders
    @PostMapping
    public ResponseEntity<?> placeOrder(
            @RequestBody(required = false) Map<String, Object> request,
            HttpSession session) {

        try {
            Long userId = getLoggedInUserId(session);

            if (request == null || request.get("addressId") == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "addressId is required");
            }

            Long addressId;

            try {
                addressId = Long.valueOf(
                        request.get("addressId").toString());

                if (addressId <= 0) {
                    throw new NumberFormatException();
                }

            } catch (NumberFormatException ex) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "addressId must be a positive integer");
            }

            Map<String, Object> result =
                    orderService.placeOrder(userId, addressId);

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(result);

        } catch (Exception ex) {

            ex.printStackTrace();

            String message = ex.getMessage();

            if (message == null || message.isBlank()) {
                message = ex.getClass().getSimpleName();
            }

            HttpStatus status = HttpStatus.BAD_REQUEST;

            if (ex instanceof ResponseStatusException) {
                status = (HttpStatus)
                        ((ResponseStatusException) ex).getStatusCode();
            }

            return ResponseEntity
                    .status(status)
                    .body(Map.of("error", message));
        }
    }

    // GET /api/orders
    @GetMapping
    public ResponseEntity<List<Order>> getOrders(
            HttpSession session) {

        Long userId = getLoggedInUserId(session);

        return ResponseEntity.ok(
                orderService.getOrders(userId));
    }
}