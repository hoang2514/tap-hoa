package com.inn.POJO;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.io.Serializable;

@NamedQuery(
        name = "CartItem.getItemsForUser",
        query = "SELECT new com.inn.wrapper.CartItemWrapper(c.id, p.id, p.name, p.description, p.price, c.quantity, p.category.id, p.category.name) " +
                "FROM CartItem c JOIN c.product p WHERE c.userEmail = :email"
)
@Data
@Entity
@DynamicInsert
@DynamicUpdate
@Table(name = "cart_item")
public class CartItem implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_fk", nullable = false)
    private Product product;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;
}

