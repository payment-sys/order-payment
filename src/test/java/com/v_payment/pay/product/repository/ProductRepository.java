package com.v_payment.pay.product.repository;

import com.v_payment.pay.product.domain.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;

/** Test-only repository to keep the order project independent from the product project. */
public interface ProductRepository extends JpaRepository<Product, Long> {
}