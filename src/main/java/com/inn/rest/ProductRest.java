package com.inn.rest;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.inn.wrapper.ProductWrapper;

@RequestMapping(path = "/product")
public interface ProductRest {

    @PostMapping(path = "/add")
    ResponseEntity<String> addNewProduct(@RequestBody Map<String, String> requestMap);

    @GetMapping(path = "/get")
    ResponseEntity<List<ProductWrapper>> getAllProduct();

    @PostMapping(path = "/update")
    ResponseEntity<String> updateProduct(@RequestBody Map<String, String> requestMap);

    
}