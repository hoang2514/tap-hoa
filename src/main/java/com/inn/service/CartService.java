package com.inn.service;

import com.inn.wrapper.CartItemWrapper;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

public interface CartService {

    ResponseEntity<List<CartItemWrapper>> getCartItems();

    ResponseEntity<?> addToCart(Map<String, Object> requestMap);

    ResponseEntity<?> updateCartItem(Integer cartItemId, Map<String, Object> requestMap);

    ResponseEntity<String> deleteCartItem(Integer cartItemId);

    ResponseEntity<?> clearCart();
}

