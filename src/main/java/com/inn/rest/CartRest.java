package com.inn.rest;

import com.inn.wrapper.CartItemWrapper;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RequestMapping(path = "/cart")
public interface CartRest {

    @GetMapping
    ResponseEntity<List<CartItemWrapper>> getCartItems();

    @PostMapping
    ResponseEntity<?> addToCart(@RequestBody Map<String, Object> requestMap);

    @PutMapping(path = "/{cartItemId}")
    ResponseEntity<?> updateCartItem(@PathVariable Integer cartItemId, @RequestBody Map<String, Object> requestMap);

    @DeleteMapping(path = "/{cartItemId}")
    ResponseEntity<String> deleteCartItem(@PathVariable Integer cartItemId);

    @DeleteMapping("/clear")
    public ResponseEntity<?> clearCart();
}

