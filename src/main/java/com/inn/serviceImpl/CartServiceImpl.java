package com.inn.serviceImpl;

import com.inn.JWT.JwtFilter;
import com.inn.POJO.CartItem;
import com.inn.POJO.Product;
import com.inn.constants.TaphoaConstants;
import com.inn.dao.CartItemDao;
import com.inn.dao.ProductDao;
import com.inn.service.CartService;
import com.inn.utils.TaphoaUtils;
import com.inn.wrapper.CartItemWrapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartItemDao cartItemDao;

    @Autowired
    private ProductDao productDao;

    @Autowired
    private JwtFilter jwtFilter;

    @Override
    public ResponseEntity<List<CartItemWrapper>> getCartItems() {
        try {
            String currentUser = jwtFilter.getCurrentUser();
            if (!isAuthenticatedUser()) {
                return new ResponseEntity<>(List.of(), HttpStatus.UNAUTHORIZED);
            }
            return new ResponseEntity<>(cartItemDao.getItemsForUser(currentUser), HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return new ResponseEntity<>(List.of(), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<?> addToCart(Map<String, Object> requestMap) {
        try {
            if (!isAuthenticatedUser()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", TaphoaConstants.UNAUTHORIZED_ACCESS));
            }

            String currentUser = jwtFilter.getCurrentUser();
            Integer productId = parseInteger(requestMap.get("productId"));
            Integer quantity = parseInteger(requestMap.getOrDefault("quantity", 1));

            if (productId == null || quantity == null || quantity <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", TaphoaConstants.INVALID_DATA));
            }

            Optional<Product> productOpt = productDao.findById(productId);
            if (productOpt.isEmpty() || !"true".equalsIgnoreCase(productOpt.get().getStatus())) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Sản phẩm không tồn tại."));
            }

            Product product = productOpt.get();
            if (product.getQuantity() == null || product.getQuantity() <= 0) {
                return ResponseEntity.badRequest()
                        .body(Map.of("message", "Hết hàng."));
            }

            CartItem existing = cartItemDao.findByUserEmailAndProduct_Id(currentUser, productId);
            if (existing != null) {
                Integer newQuantity = existing.getQuantity() + quantity;
                if (newQuantity > product.getQuantity()) {
                    return ResponseEntity.badRequest()
                            .body(Map.of(
                                    "message", "Số lượng vượt quá tồn kho.",
                                    "available", product.getQuantity()
                            ));
                }
                existing.setQuantity(newQuantity);
                cartItemDao.save(existing);
            } else {
                if (quantity > product.getQuantity()) {
                    return ResponseEntity.badRequest()
                            .body(Map.of(
                                    "message", "Số lượng vượt quá tồn kho.",
                                    "available", product.getQuantity()
                            ));
                }
                CartItem cartItem = new CartItem();
                cartItem.setUserEmail(currentUser);
                cartItem.setProduct(product);
                cartItem.setQuantity(quantity);
                cartItemDao.save(cartItem);
            }

            return ResponseEntity.ok(Map.of("message", "Đã thêm vào giỏ."));
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", TaphoaConstants.Something_Went_Wrong));
    }


    @Override
    public ResponseEntity<?> updateCartItem(Integer cartItemId, Map<String, Object> requestMap) {
        try {
            if (!isAuthenticatedUser()) {
                return TaphoaUtils.getResponseEntity(TaphoaConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
            String currentUser = jwtFilter.getCurrentUser();
            Integer quantity = parseInteger(requestMap.get("quantity"));
            if (cartItemId == null || quantity == null || quantity <= 0) {
                return TaphoaUtils.getResponseEntity(TaphoaConstants.INVALID_DATA, HttpStatus.BAD_REQUEST);
            }
            Optional<CartItem> cartItemOpt = cartItemDao.findByIdAndUserEmail(cartItemId, currentUser);
            if (cartItemOpt.isEmpty()) {
                return TaphoaUtils.getResponseEntity("Cart item not found", HttpStatus.NOT_FOUND);
            }
            CartItem cartItem = cartItemOpt.get();
            Product product = cartItem.getProduct();

            // Kiểm tra số lượng hàng trong kho
            if (product.getQuantity() == null || quantity > product.getQuantity()) {
                return ResponseEntity.badRequest().body(
                        Map.of(
                                "message", "Số lượng vượt quá tồn kho",
                                "available", product.getQuantity()
                        )
                );
            }
            cartItem.setQuantity(quantity);
            cartItemDao.save(cartItem);
            return TaphoaUtils.getResponseEntity("Cart item updated", HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return TaphoaUtils.getResponseEntity(TaphoaConstants.Something_Went_Wrong, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<String> deleteCartItem(Integer cartItemId) {
        try {
            if (!isAuthenticatedUser()) {
                return TaphoaUtils.getResponseEntity(TaphoaConstants.UNAUTHORIZED_ACCESS, HttpStatus.UNAUTHORIZED);
            }
            String currentUser = jwtFilter.getCurrentUser();
            Optional<CartItem> cartItemOpt = cartItemDao.findByIdAndUserEmail(cartItemId, currentUser);
            if (cartItemOpt.isEmpty()) {
                return TaphoaUtils.getResponseEntity("Cart item not found", HttpStatus.NOT_FOUND);
            }
            cartItemDao.delete(cartItemOpt.get());
            return TaphoaUtils.getResponseEntity("Cart item deleted", HttpStatus.OK);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return TaphoaUtils.getResponseEntity(TaphoaConstants.Something_Went_Wrong, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @Override
    public ResponseEntity<?> clearCart() {
        try {
            if (!isAuthenticatedUser()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(Map.of("message", TaphoaConstants.UNAUTHORIZED_ACCESS));
            }

            String currentUser = jwtFilter.getCurrentUser();
            List<CartItemWrapper> items = cartItemDao.getItemsForUser(currentUser);

            if (!items.isEmpty()) {
                items.forEach(wrapper -> cartItemDao.deleteById(wrapper.getCartItemId()));
            }

            return ResponseEntity.ok(Map.of("message", "Đã xoá toàn bộ giỏ hàng"));
        } catch (Exception ex) {
            ex.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", TaphoaConstants.Something_Went_Wrong));
        }
    }


    private Integer parseInteger(Object value) {
        try {
            if (value == null) {
                return null;
            }
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    private boolean isAuthenticatedUser() {
        return jwtFilter.getCurrentUser() != null && (jwtFilter.isUser() || jwtFilter.isAdmin());
    }
}

