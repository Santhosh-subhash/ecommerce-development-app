
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
import com.example.ecommerce.entity.Order;
import com.example.ecommerce.entity.OrderItem;
import com.example.ecommerce.entity.Product;
import com.example.ecommerce.repository.CartItemRepository;
import com.example.ecommerce.repository.CartRepository;
import com.example.ecommerce.repository.AddressRepository;
import com.example.ecommerce.entity.Address;
import com.example.ecommerce.repository.OrderItemRepository;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.ProductRepository;

@Service
public class OrderService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final AddressRepository addressRepository;

    public OrderService(
            CartRepository cartRepository,
            CartItemRepository cartItemRepository,
            OrderRepository orderRepository,
            OrderItemRepository orderItemRepository,
            ProductRepository productRepository,
            AddressRepository addressRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.productRepository = productRepository;
        this.addressRepository = addressRepository;
    }

    @Transactional
    public Map<String, Object> placeOrder(Long userId, Long addressId) {
        if (userId == null || userId <= 0) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in before checkout");
        }
        if (addressId == null || addressId <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A valid shipping address is required");
        }
        Address address = addressRepository.findById(addressId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Shipping address not found"));
        if (!userId.equals(address.getUserId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "You cannot use another user's shipping address");
        }

    	Cart cart = cartRepository
    	        .findFirstByUserIdAndStatus(userId, "ACTIVE")
    	        .orElseThrow(() -> new ResponseStatusException(
    	                HttpStatus.BAD_REQUEST, "Active cart not found"));

        List<CartItem> cartItems =
                cartItemRepository.findByCartId(cart.getCartId());

        if (cartItems.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Your cart is empty");
        }

        BigDecimal subtotal = BigDecimal.ZERO;

        Order order = new Order();
        order.setUserId(userId);
        order.setAddressId(addressId);
        order.setStatus("PLACED");
        order.setSubtotal(BigDecimal.ZERO);
        order.setShippingFee(BigDecimal.ZERO);
        order.setTaxAmount(BigDecimal.ZERO);
        order.setTotalAmount(BigDecimal.ZERO);

        order = orderRepository.save(order);

        for (CartItem cartItem : cartItems) {

            Product product = productRepository
                    .findById(cartItem.getProductId())
                    .orElseThrow(() -> new ResponseStatusException(
                            HttpStatus.NOT_FOUND, "Product not found"));

            int quantity = cartItem.getQuantity();

            if (quantity <= 0
                    || product.getStockQuantity() == null
                    || product.getStockQuantity() < quantity) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Insufficient stock for " + product.getProductName());
            }

            BigDecimal unitPrice = product.getPrice();
            BigDecimal lineTotal =
                    unitPrice.multiply(BigDecimal.valueOf(quantity));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrderId(order.getOrderId());
            orderItem.setProductId(product.getProductId());
            orderItem.setQuantity(quantity);
            orderItem.setUnitPrice(unitPrice);
            orderItem.setLineTotal(lineTotal);

            orderItemRepository.save(orderItem);

            product.setStockQuantity(
                    product.getStockQuantity() - quantity);
            productRepository.save(product);

            subtotal = subtotal.add(lineTotal);
        }

        order.setSubtotal(subtotal);
        BigDecimal shippingFee = BigDecimal.ZERO;
        BigDecimal taxAmount = subtotal.multiply(new BigDecimal("0.05")).setScale(2, java.math.RoundingMode.HALF_UP);
        order.setShippingFee(shippingFee);
        order.setTaxAmount(taxAmount);
        order.setTotalAmount(subtotal.add(taxAmount));

        orderRepository.save(order);

        cartItemRepository.deleteByCartId(cart.getCartId());

        cart.setStatus("CHECKED_OUT");
        cartRepository.save(cart);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("orderId", order.getOrderId());
        response.put("status", order.getStatus());
        response.put("subtotal", order.getSubtotal());
        response.put("shippingFee", order.getShippingFee());
        response.put("taxAmount", order.getTaxAmount());
        response.put("totalAmount", order.getTotalAmount());
        response.put("message", "Order placed successfully");

        return response;
    }

    @Transactional(readOnly = true)
    public List<Order> getOrders(Long userId) {
        return orderRepository.findByUserId(userId);
    }
}
