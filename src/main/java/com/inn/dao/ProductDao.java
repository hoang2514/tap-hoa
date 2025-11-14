package com.inn.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.inn.POJO.Product;
import com.inn.wrapper.ProductWrapper;
import org.springframework.data.repository.query.Param;

public interface ProductDao extends JpaRepository<Product, Integer> {

    List<ProductWrapper> getAllProduct();

    void updateProductStatus(@Param("status") String status, @Param("id") Integer id);

}