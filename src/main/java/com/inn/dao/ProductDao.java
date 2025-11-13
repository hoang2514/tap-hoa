package com.inn.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import com.inn.POJO.Product;

public interface ProductDao extends JpaRepository<Product, Integer> {
    
}
