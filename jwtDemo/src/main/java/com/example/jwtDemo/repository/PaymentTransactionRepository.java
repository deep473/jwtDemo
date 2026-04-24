package com.example.jwtDemo.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.jwtDemo.entity.PaymentTransaction;

public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, Long> {
}