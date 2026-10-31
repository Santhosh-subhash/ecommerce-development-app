
package com.example.ecommerce.service;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.ecommerce.entity.Cart;
import com.example.ecommerce.entity.CartItem;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.repository.CartItemRepository;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.ProductRepository;

@Service
public class CartService {

    private static final int MAX_QUANTITY_PER_PRODUCT = 99;
    private static final int MAX_DISTINCT_PRODUCTS = 50;

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            ProductRepository productRepository) {

        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    // Get an ACTIVE cart or create a new one
    private Cart getOrCreateCart(Long userId) {

        validateUser(userId);

        return cartRepository
                .findFirstByUserIdAndStatus(userId, "ACTIVE")
                .orElseGet(() -> {
                    Cart cart = new Cart();
                    cart.setUserId(userId);
                    cart.setStatus("ACTIVE");
                    return cartRepository.save(cart);
                });
    }

    private void validateUser(Long userId) {

        if (userId == null || userId <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Please log in first");
        }
    }

    // Get an active product
    private Product getActiveProduct(Long productId) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Product not found"));

        if (product.getIsActive() != null && !"Y".equalsIgnoreCase(product.getIsActive())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Product is not active");
        }

        return product;
    }

    // Get current user's cart
    @Transactional(readOnly = true)
    public Map<String, Object> getCart(Long userId) {

        validateUser(userId);

        Cart cart = cartRepository
                .findFirstByUserIdAndStatus(userId, "ACTIVE")
                .orElse(null);

        if (cart == null) {
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("cartId", null);
            result.put("items", List.of());
            result.put("total", BigDecimal.ZERO);
            result.put("itemCount", 0);
            return result;
        }

        return buildCartResponse(cart);
    }

    // Add a product to the active cart
    @Transactional
    public Map<String, Object> addItem(
            Long userId,
            Long productId,
            Integer quantity) {

        validateUser(userId);

        if (productId == null || quantity == null || quantity <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Valid productId and positive quantity are required");
        }

        Product product = getActiveProduct(productId);
        Cart cart = getOrCreateCart(userId);

        CartItem item = cartItemRepository
                .findByCartIdAndProductId(
                        cart.getCartId(),
                        productId)
                .orElse(null);

        int newQuantity = quantity;

        if (item != null) {
            newQuantity += item.getQuantity();
        }

        if (newQuantity > MAX_QUANTITY_PER_PRODUCT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Maximum allowed quantity per product is " + MAX_QUANTITY_PER_PRODUCT);
        }
        validateStock(product, newQuantity);
        if (item == null && cartItemRepository.findByCartId(cart.getCartId()).size() >= MAX_DISTINCT_PRODUCTS) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Cart supports up to " + MAX_DISTINCT_PRODUCTS + " different products");
        }

        if (item == null) {
            item = new CartItem();
            item.setCartId(cart.getCartId());
            item.setProductId(productId);
        }

        item.setQuantity(newQuantity);
        item.setUnitPrice(product.getPrice());

        cartItemRepository.save(item);
        cartItemRepository.flush();

        return buildCartResponse(cart);
    }

    // Update cart item quantity
    @Transactional
    public Map<String, Object> updateItem(
            Long userId,
            Long cartItemId,
            Integer quantity) {

        validateUser(userId);

        if (cartItemId == null || quantity == null || quantity <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Valid cartItemId and positive quantity are required");
        }

        Cart cart = getOrCreateCart(userId);

        CartItem item = cartItemRepository
                .findByCartItemIdAndCartId(
                        cartItemId,
                        cart.getCartId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cart item not found"));

        Product product = getActiveProduct(item.getProductId());

        if (quantity > MAX_QUANTITY_PER_PRODUCT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Maximum allowed quantity per product is " + MAX_QUANTITY_PER_PRODUCT);
        }
        validateStock(product, quantity);

        item.setQuantity(quantity);
        item.setUnitPrice(product.getPrice());

        cartItemRepository.save(item);

        return buildCartResponse(cart);
    }

    // Remove an item from the active cart
    @Transactional
    public Map<String, Object> removeItem(
            Long userId,
            Long cartItemId) {

        validateUser(userId);

        Cart cart = cartRepository
                .findFirstByUserIdAndStatus(userId, "ACTIVE")
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Active cart not found"));

        CartItem item = cartItemRepository
                .findByCartItemIdAndCartId(
                        cartItemId,
                        cart.getCartId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cart item not found"));

        cartItemRepository.delete(item);

        return buildCartResponse(cart);
    }

    // Clear active cart
    @Transactional
    public Map<String, Object> clearCart(Long userId) {

        validateUser(userId);

        Cart cart = cartRepository
                .findFirstByUserIdAndStatus(userId, "ACTIVE")
                .orElse(null);

        if (cart != null) {
            cartItemRepository.deleteByCartId(cart.getCartId());
        }

        return getCart(userId);
    }

    // Validate stock availability
    private void validateStock(Product product, int quantity) {

        Integer stock = product.getStockQuantity();

        if (stock == null || stock < quantity) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Insufficient stock. Available quantity: "
                            + (stock == null ? 0 : stock));
        }
    }

    // Build cart response for frontend
    private Map<String, Object> buildCartResponse(Cart cart) {

        List<CartItem> cartItems =
                cartItemRepository.findByCartId(cart.getCartId());

        List<Map<String, Object>> items = cartItems.stream()
                .map(item -> {

                    Map<String, Object> row = new LinkedHashMap<>();

                    Product product = productRepository
                            .findById(item.getProductId())
                            .orElse(null);

                    row.put("cartItemId", item.getCartItemId());
                    row.put("productId", item.getProductId());
                    row.put("quantity", item.getQuantity());
                    row.put("unitPrice", item.getUnitPrice());

                    if (product != null) {
                        row.put("productName", product.getProductName());
                        row.put("imageUrl", product.getImageUrl());
                        row.put("stockQuantity", product.getStockQuantity());
                    }

                    BigDecimal lineTotal = item.getUnitPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity()));

                    row.put("lineTotal", lineTotal);

                    return row;
                })
                .toList();

        BigDecimal total = items.stream()
                .map(item -> (BigDecimal) item.get("lineTotal"))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        int itemCount = items.stream()
                .mapToInt(item -> (Integer) item.get("quantity"))
                .sum();

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("cartId", cart.getCartId());
        response.put("items", items);
        response.put("total", total);
        response.put("itemCount", itemCount);

        return response;
    }
}
