package com.example.jwtDemo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.jwtDemo.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
}