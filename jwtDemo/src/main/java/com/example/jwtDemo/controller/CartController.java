package com.example.jwtDemo.controller;

import java.util.Map;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.jwtDemo.dto.AddToCartRequest;
import com.example.jwtDemo.dto.CartResponse;
import com.example.jwtDemo.dto.UpdateCartRequest;
import com.example.jwtDemo.service.CartService;

@RestController
@RequestMapping("/customer/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PostMapping
    public ResponseEntity<Map<String, String>> addToCart(@Valid @RequestBody AddToCartRequest request) {
        String message = cartService.addToCart(request);
        return ResponseEntity.ok(Map.of("message", message));
    }

    @GetMapping
    public ResponseEntity<CartResponse> getCart() {
        return ResponseEntity.ok(cartService.getCart());
    }

    @PutMapping("/{cartItemId}")
    public ResponseEntity<Map<String, String>> updateCartItem(@PathVariable Long cartItemId,
                                                              @Valid @RequestBody UpdateCartRequest request) {
        String message = cartService.updateCartItem(cartItemId, request);
        return ResponseEntity.ok(Map.of("message", message));
    }

    @DeleteMapping("/{cartItemId}")
    public ResponseEntity<Map<String, String>> removeCartItem(@PathVariable Long cartItemId) {
        String message = cartService.removeCartItem(cartItemId);
        return ResponseEntity.ok(Map.of("message", message));
    }
}