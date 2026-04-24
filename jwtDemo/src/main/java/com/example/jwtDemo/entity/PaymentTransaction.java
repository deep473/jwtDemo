package com.example.jwtDemo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "payment_transactions")
public class PaymentTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(optional = false)
    @JoinColumn(name = "order_id", unique = true)
    private PurchaseOrder order;

    @Column(unique = true)
    private String razorpayPaymentId;

    private String razorpayOrderId;

    @Column(length = 500)
    private String razorpaySignature;

    @Column(nullable = false)
    private String status;

    public PaymentTransaction() {
    }

    public PaymentTransaction(PurchaseOrder order, String razorpayPaymentId,
                              String razorpayOrderId, String razorpaySignature, String status) {
        this.order = order;
        this.razorpayPaymentId = razorpayPaymentId;
        this.razorpayOrderId = razorpayOrderId;
        this.razorpaySignature = razorpaySignature;
        this.status = status;
    }

    public Long getId() {
        return id;
    }

    public PurchaseOrder getOrder() {
        return order;
    }

    public String getRazorpayPaymentId() {
        return razorpayPaymentId;
    }

    public String getRazorpayOrderId() {
        return razorpayOrderId;
    }

    public String getRazorpaySignature() {
        return razorpaySignature;
    }

    public String getStatus() {
        return status;
    }

    public void setOrder(PurchaseOrder order) {
        this.order = order;
    }

    public void setRazorpayPaymentId(String razorpayPaymentId) {
        this.razorpayPaymentId = razorpayPaymentId;
    }

    public void setRazorpayOrderId(String razorpayOrderId) {
        this.razorpayOrderId = razorpayOrderId;
    }

    public void setRazorpaySignature(String razorpaySignature) {
        this.razorpaySignature = razorpaySignature;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}