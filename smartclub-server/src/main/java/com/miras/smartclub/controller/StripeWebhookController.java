package com.miras.smartclub.controller;
import com.miras.smartclub.service.ReservationService;
import com.stripe.model.Event;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;
import java.util.*;

@RestController
@RequestMapping("/api/webhook")
@RequiredArgsConstructor
public class StripeWebhookController {

    private final ReservationService reservationService;
    
    @Value("${stripe.webhook.secret}")
    private String webhookSecret;

    @PostMapping("/stripe")
    public Map<String, String> handleStripeWebhook(
            @RequestBody String payload,
            @RequestHeader("Stripe-Signature") String sigHeader) {

        System.out.println("🔥 STRIPE WEBHOOK HIT 🔥");
        System.out.println("Raw Stripe payload: " + payload);

        try {
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            System.out.println("Event type: " + event.getType());

            if ("checkout.session.completed".equals(event.getType())) {
                com.google.gson.JsonParser parser = new com.google.gson.JsonParser();
                com.google.gson.JsonObject json = parser.parse(payload).getAsJsonObject();
                com.google.gson.JsonObject data = json.getAsJsonObject("data");
                com.google.gson.JsonObject object = data.getAsJsonObject("object");
                com.google.gson.JsonObject metadata = object.getAsJsonObject("metadata");
                
                String reservationId = metadata.get("reservationId").getAsString();
                String paymentIntentId = object.get("payment_intent").getAsString();

                System.out.println("✅ reservationId = " + reservationId);
                System.out.println("✅ paymentIntentId = " + paymentIntentId);

                reservationService.activateReservation(reservationId, paymentIntentId);
            }

            return Map.of("status", "success");

        } catch (Exception e) {
            System.err.println("❌ Webhook error: " + e.getMessage());
            e.printStackTrace();
            return Map.of("status", "error", "message", e.getMessage());
        }
    } 
}