package com.inn.wrapper;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CartItemWrapper {
    private Integer cartItemId;
    private Integer productId;
    private String productName;
    private String productDescription;
    private Float price;
    private Integer quantity;
    private Integer categoryId;
    private String categoryName;

    public Float getSubTotal() {
        if (price == null || quantity == null) {
            return 0f;
        }
        return price * quantity;
    }
}

