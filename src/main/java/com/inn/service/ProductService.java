package com.inn.service;

import java.util.List;
import java.util.Map;

import jdk.javadoc.doclet.Reporter;
import org.springframework.http.ResponseEntity;

import com.inn.wrapper.ProductWrapper;

public interface ProductService {

    ResponseEntity<String> addNewProduct(Map<String, String> requestMap);

    List<ProductWrapper> getAllProduct();

    ResponseEntity<String> updateProduct(Map<String, String> requestMap);

    ResponseEntity<String> deleteProduct(Integer id);

    ResponseEntity<String> updateStatus(Map<String, String> requestMap);

    List<ProductWrapper> getByCategory(Integer id);

    ProductWrapper getProductById(Integer id);
}