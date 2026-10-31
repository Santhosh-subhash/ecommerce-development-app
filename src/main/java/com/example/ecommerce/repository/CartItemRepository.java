
package com.example.ecommerce.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.ecommerce.entity.CartItem;

public interface CartItemRepository
        extends JpaRepository<CartItem, Long> {

    List<CartItem> findByCartId(Long cartId);

    Optional<CartItem> findByCartIdAndProductId(
            Long cartId, Long productId);

    Optional<CartItem> findByCartItemIdAndCartId(
            Long cartItemId, Long cartId);

    void deleteByCartId(Long cartId);
}
