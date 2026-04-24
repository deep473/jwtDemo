package com.example.jwtDemo.service;

import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class RazorpayService {

    private final RestClient restClient;

    @Value("${razorpay.key-id}")
    private String keyId;

    @Value("${razorpay.key-secret}")
    private String keySecret;

    @Value("${razorpay.currency}")
    private String currency;

    public RazorpayService(@Value("${razorpay.key-id}") String keyId,
                           @Value("${razorpay.key-secret}") String keySecret) {
        this.restClient = RestClient.builder()
                .baseUrl("https://api.razorpay.com/v1")
                .defaultHeaders(headers -> headers.setBasicAuth(keyId, keySecret))
                .build();
    }

    public RazorpayOrderResponse createOrder(long amount, String receipt) {
        return restClient.post()
                .uri("/orders")
                .body(Map.of(
                        "amount", amount,
                        "currency", currency,
                        "receipt", receipt
                ))
                .retrieve()
                .body(RazorpayOrderResponse.class);
    }

    public boolean verifySignature(String serverOrderId, String razorpayPaymentId, String razorpaySignature) {
        try {
            String payload = serverOrderId + "|" + razorpayPaymentId;
            Mac mac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec =
                    new SecretKeySpec(keySecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            mac.init(secretKeySpec);

            byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String generatedSignature = HexFormat.of().formatHex(digest);

            return generatedSignature.equals(razorpaySignature);
        } catch (Exception e) {
            throw new RuntimeException("Error while verifying Razorpay signature", e);
        }
    }

    public String getKeyId() {
        return keyId;
    }

    public String getCurrency() {
        return currency;
    }

    public record RazorpayOrderResponse(
            String id,
            String entity,
            Integer amount,
            Integer amount_paid,
            Integer amount_due,
            String currency,
            String receipt,
            String status
    ) {
    }
}