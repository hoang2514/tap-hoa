package com.inn.wrapper;
import java.io.Serializable;

import lombok.Data;

@Data
// Implements Serializable for storing in Redis
public class ProductWrapper implements Serializable {

    private Integer id;
    private String name;
    private String description;
    private Float price;
    private String status;
    private Integer quantity;
    private Integer categoryId;
    private String categoryName;
    private String imageUrl;

    public ProductWrapper() {

    }

    public ProductWrapper(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public ProductWrapper(Integer id, String name, String description, Float price, Integer quantity) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.quantity = quantity;
    }

    public ProductWrapper(Integer id, String name, String description, Float price, String status, Integer quantity, Integer categoryId, String categoryName, String imageUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.status = status;
        this.quantity = quantity;  
        this.categoryId = categoryId;
        this.categoryName = categoryName;
        this.imageUrl = imageUrl;
    }
}