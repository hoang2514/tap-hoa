package com.inn.dao;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.inn.POJO.Product;
import com.inn.wrapper.ProductWrapper;

public interface ProductDao extends JpaRepository<Product, Integer> {

    List<ProductWrapper> getAllProduct();

}