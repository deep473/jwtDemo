package com.example.jwtDemo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record VerifyPaymentRequest(
        @NotNull Long localOrderId,
        @NotBlank String razorpayPaymentId,
        @NotBlank String razorpayOrderId,
        @NotBlank String razorpaySignature
) {
}