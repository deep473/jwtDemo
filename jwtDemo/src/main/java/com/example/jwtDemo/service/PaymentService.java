package com.example.jwtDemo.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.jwtDemo.dto.CreatePaymentOrderResponse;
import com.example.jwtDemo.dto.VerifyPaymentRequest;
import com.example.jwtDemo.entity.CartItem;
import com.example.jwtDemo.entity.OrderItem;
import com.example.jwtDemo.entity.PaymentTransaction;
import com.example.jwtDemo.entity.Product;
import com.example.jwtDemo.entity.PurchaseOrder;
import com.example.jwtDemo.entity.User;
import com.example.jwtDemo.repository.CartItemRepository;
import com.example.jwtDemo.repository.OrderItemRepository;
import com.example.jwtDemo.repository.PaymentTransactionRepository;
import com.example.jwtDemo.repository.ProductRepository;
import com.example.jwtDemo.repository.PurchaseOrderRepository;
import com.example.jwtDemo.repository.UserRepository;

@Service
public class PaymentService {

    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final OrderItemRepository orderItemRepository;
    private final PaymentTransactionRepository paymentTransactionRepository;
    private final RazorpayService razorpayService;

    public PaymentService(CartItemRepository cartItemRepository,
                          UserRepository userRepository,
                          ProductRepository productRepository,
                          PurchaseOrderRepository purchaseOrderRepository,
                          OrderItemRepository orderItemRepository,
                          PaymentTransactionRepository paymentTransactionRepository,
                          RazorpayService razorpayService) {
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.orderItemRepository = orderItemRepository;
        this.paymentTransactionRepository = paymentTransactionRepository;
        this.razorpayService = razorpayService;
    }

    @Transactional
    public CreatePaymentOrderResponse createPaymentOrder() {
        User user = getCurrentUser();
        List<CartItem> cartItems = cartItemRepository.findByUser(user);

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();

            if (cartItem.getQuantity() > product.getStock()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            BigDecimal subtotal = product.getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));

            totalAmount = totalAmount.add(subtotal);
        }

        PurchaseOrder order = new PurchaseOrder(user, totalAmount, razorpayService.getCurrency(), "CREATED");
        purchaseOrderRepository.save(order);

        long amountInPaise = toPaise(totalAmount);
        String receipt = "receipt_" + order.getId();

        RazorpayService.RazorpayOrderResponse razorpayOrder =
                razorpayService.createOrder(amountInPaise, receipt);

        order.setRazorpayOrderId(razorpayOrder.id());
        order.setStatus("PENDING_PAYMENT");
        purchaseOrderRepository.save(order);

        return new CreatePaymentOrderResponse(
                order.getId(),
                razorpayOrder.id(),
                amountInPaise,
                razorpayService.getCurrency(),
                razorpayService.getKeyId()
        );
    }

    @Transactional
    public String verifyPayment(VerifyPaymentRequest request) {
        User user = getCurrentUser();

        PurchaseOrder order = purchaseOrderRepository.findByIdAndUser(request.localOrderId(), user)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        if ("PAID".equals(order.getStatus())) {
            return "Payment already verified and order already placed";
        }

        if (!order.getRazorpayOrderId().equals(request.razorpayOrderId())) {
            throw new RuntimeException("Razorpay order id does not match");
        }

        boolean validSignature = razorpayService.verifySignature(
                order.getRazorpayOrderId(),
                request.razorpayPaymentId(),
                request.razorpaySignature()
        );

        if (!validSignature) {
            paymentTransactionRepository.save(new PaymentTransaction(
                    order,
                    request.razorpayPaymentId(),
                    request.razorpayOrderId(),
                    request.razorpaySignature(),
                    "FAILED"
            ));
            throw new RuntimeException("Invalid payment signature");
        }

        List<CartItem> cartItems = cartItemRepository.findByUser(user);

        if (cartItems.isEmpty()) {
            throw new RuntimeException("Cart is empty");
        }

        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();

            if (cartItem.getQuantity() > product.getStock()) {
                throw new RuntimeException("Insufficient stock for product: " + product.getName());
            }

            OrderItem orderItem = new OrderItem(
                    order,
                    product,
                    cartItem.getQuantity(),
                    product.getPrice()
            );
            orderItemRepository.save(orderItem);

            product.setStock(product.getStock() - cartItem.getQuantity());
            productRepository.save(product);
        }

        paymentTransactionRepository.save(new PaymentTransaction(
                order,
                request.razorpayPaymentId(),
                request.razorpayOrderId(),
                request.razorpaySignature(),
                "SUCCESS"
        ));

        order.setStatus("PAID");
        purchaseOrderRepository.save(order);

        cartItemRepository.deleteByUser(user);

        return "Payment verified and order placed successfully";
    }

    private User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Logged in user not found"));
    }

    private long toPaise(BigDecimal amount) {
        return amount.multiply(BigDecimal.valueOf(100))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
    }
}