package com.miras.smartclub.controller;
import com.miras.smartclub.model.Reservation;
import com.miras.smartclub.service.ReservationService;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.checkout.Session;
import com.stripe.param.checkout.SessionCreateParams;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpSession;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/payment")
@RequiredArgsConstructor
public class PaymentController {

    private final ReservationService reservationService;

    @Value("${stripe.api.key:your_stripe_secret_key}")
    private String stripeApiKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeApiKey;
    }

    @Data
    public static class CreatePaymentIntentRequest {
        private String reservationId;
        private Integer amount;
        private String currency;
    }

    @PostMapping("/create-intent")
    public ResponseEntity<?> createPaymentIntent(
            @RequestBody CreatePaymentIntentRequest request,
            HttpSession httpSession
    ) {
        String userId = (String) httpSession.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        if (request.getReservationId() == null || request.getAmount() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "reservationId and amount required"));
        }

        // Verify reservation belongs to user
        Optional<Reservation> maybeRes = reservationService.getById(request.getReservationId());
        if (maybeRes.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Reservation not found"));
        }

        Reservation res = maybeRes.get();
        if (!userId.equals(res.getUserId())) {
            return ResponseEntity.status(403).body(Map.of("error", "Not your reservation"));
        }

        try {
            // Create Stripe Checkout Session
            String currency = request.getCurrency() != null ? request.getCurrency() : "kzt";
            
            SessionCreateParams params = SessionCreateParams.builder()
                .setMode(SessionCreateParams.Mode.PAYMENT)
                .setSuccessUrl("http://localhost:5173/payment/success?session_id={CHECKOUT_SESSION_ID}")
                .setCancelUrl("http://localhost:5173/payment/cancel")
                .addLineItem(
                    SessionCreateParams.LineItem.builder()
                        .setPriceData(
                            SessionCreateParams.LineItem.PriceData.builder()
                                .setCurrency(currency)
                                .setUnitAmount((long) request.getAmount() * 100) // Stripe uses cents
                                .setProductData(
                                    SessionCreateParams.LineItem.PriceData.ProductData.builder()
                                        .setName("Бронирование компьютерного клуба")
                                        .setDescription("Reservation ID: " + request.getReservationId())
                                        .build()
                                )
                                .build()
                        )
                        .setQuantity(1L)
                        .build()
                )
                .putMetadata("reservationId", request.getReservationId())
                .putMetadata("userId", userId)
                .build();

            Session session = Session.create(params);

            Map<String, Object> response = new HashMap<>();
            response.put("sessionId", session.getId());
            response.put("checkoutUrl", session.getUrl());
            response.put("clientSecret", session.getClientSecret());

            return ResponseEntity.ok(response);

        } catch (StripeException e) {
            System.err.println("Stripe error: " + e.getMessage());
            return ResponseEntity.status(500).body(Map.of("error", "Payment processing error: " + e.getMessage()));
        }
    }
    @PostMapping("/verify")
    public ResponseEntity<?> verifyPayment(
            @RequestBody Map<String, String> request,
            HttpSession httpSession
    ) {
        String userId = (String) httpSession.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }

        String reservationId = request.get("reservationId");
        if (reservationId != null) {
            Optional<Reservation> maybeRes = reservationService.getById(reservationId);
            if (maybeRes.isPresent()) {
                return ResponseEntity.ok(Map.of(
                    "success", true,
                    "reservation", maybeRes.get()
                ));
            }
        }

        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/success")
    public ResponseEntity<?> paymentSuccess(@RequestParam(required = false) String session_id) {
        // Handle successful payment
        // You can verify the session with Stripe and update reservation status
        return ResponseEntity.ok(Map.of("message", "Payment successful", "sessionId", session_id));
    }

    @GetMapping("/cancel")
    public ResponseEntity<?> paymentCancel() {
        return ResponseEntity.ok(Map.of("message", "Payment cancelled"));
    }
}