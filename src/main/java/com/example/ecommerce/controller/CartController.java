
package com.example.ecommerce.controller;

import java.util.Map;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.ecommerce.service.CartService;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    private Long getLoggedInUserId(HttpSession session) {

        Object value = session.getAttribute("userId");

        if (value == null) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Please log in first");
        }

        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException ex) {
            session.invalidate();

            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid session. Please log in again");
        }
    }

    @GetMapping
    public ResponseEntity<Map<String, Object>> getCart(
            HttpSession session) {

        Long userId = getLoggedInUserId(session);

        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @PostMapping("/items")
    public ResponseEntity<Map<String, Object>> addItem(
            @RequestBody Map<String, Object> body,
            HttpSession session) {

        Long userId = getLoggedInUserId(session);

        Long productId = parseLong(body.get("productId"), "productId");
        Integer quantity = parseInteger(body.get("quantity"), "quantity");

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(cartService.addItem(userId, productId, quantity));
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<Map<String, Object>> updateItem(
            @PathVariable Long id,
            @RequestBody Map<String, Object> body,
            HttpSession session) {

        Long userId = getLoggedInUserId(session);
        Integer quantity = parseInteger(body.get("quantity"), "quantity");

        return ResponseEntity.ok(
                cartService.updateItem(userId, id, quantity));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<Map<String, Object>> removeItem(
            @PathVariable Long id,
            HttpSession session) {

        Long userId = getLoggedInUserId(session);

        return ResponseEntity.ok(
                cartService.removeItem(userId, id));
    }

    @DeleteMapping
    public ResponseEntity<Map<String, Object>> clearCart(
            HttpSession session) {

        Long userId = getLoggedInUserId(session);

        return ResponseEntity.ok(cartService.clearCart(userId));
    }

    private Long parseLong(Object value, String field) {

        try {
            if (value == null) {
                throw new NumberFormatException();
            }

            return Long.valueOf(value.toString());
        } catch (NumberFormatException ex) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    field + " must be a valid integer");
        }
    }

    private Integer parseInteger(Object value, String field) {

        try {
            if (value == null) {
                throw new NumberFormatException();
            }

            return Integer.valueOf(value.toString());
        } catch (NumberFormatException ex) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    field + " must be a valid integer");
        }
    }
}
