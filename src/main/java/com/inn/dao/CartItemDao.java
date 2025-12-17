package com.inn.dao;

import com.inn.POJO.CartItem;
import com.inn.wrapper.CartItemWrapper;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemDao extends JpaRepository<CartItem, Integer> {

    @Query(name = "CartItem.getItemsForUser")
    List<CartItemWrapper> getItemsForUser(@Param("email") String email);

    CartItem findByUserEmailAndProduct_Id(String userEmail, Integer productId);

    Optional<CartItem> findByIdAndUserEmail(Integer id, String userEmail);
}

