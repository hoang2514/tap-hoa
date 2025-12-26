package com.inn.wrapper;
import java.io.Serializable;

import lombok.Data;

@Data
// Implements Serializable for storing in Redis
public class ProductWrapper implements Serializable {

    Integer id;
    String name;
    String description;
    Float price;
    String status;
    Integer categoryId;
    String categoryName;
    String imageUrl;

    public ProductWrapper() {

    }

    public ProductWrapper(Integer id, String name) {
        this.id = id;
        this.name = name;
    }

    public ProductWrapper(Integer id, String name, String description, Float price) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
    }

    public ProductWrapper(Integer id, String name, String description, Float price, String status, Integer categoryId, String categoryName, String imageUrl) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.price = price;
        this.status = status;
        this.imageUrl = imageUrl;
        this.categoryId = categoryId;
        this.categoryName = categoryName;
    }
}