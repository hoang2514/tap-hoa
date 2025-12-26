package com.inn.restImpl;

import com.inn.rest.CartRest;
import com.inn.service.CartService;
import com.inn.wrapper.CartItemWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class CartRestImpl implements CartRest {

    @Autowired
    private CartService cartService;

    @Override
    public ResponseEntity<List<CartItemWrapper>> getCartItems() {
        return cartService.getCartItems();
    }

    @Override
    public ResponseEntity<String> addToCart(Map<String, Object> requestMap) {
        return cartService.addToCart(requestMap);
    }

    @Override
    public ResponseEntity<String> updateCartItem(Integer cartItemId, Map<String, Object> requestMap) {
        return cartService.updateCartItem(cartItemId, requestMap);
    }

    @Override
    public ResponseEntity<String> deleteCartItem(Integer cartItemId) {
        return cartService.deleteCartItem(cartItemId);
    }
}

