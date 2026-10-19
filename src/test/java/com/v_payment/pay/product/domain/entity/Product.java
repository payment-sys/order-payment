package com.v_payment.pay.product.domain.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private Long price;
    private Integer stockQuantity;

    protected Product() {}

    private Product(String name, Long price, Integer stockQuantity) {
        this.name = name;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }

    public static Product create(String name, Long price, Integer stockQuantity) {
        return new Product(name, price, stockQuantity);
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public Long getPrice() { return price; }
}