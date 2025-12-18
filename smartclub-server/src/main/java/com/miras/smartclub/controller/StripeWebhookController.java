package com.miras.smartclub.controller;

import com.miras.smartclub.model.Reservation;
import com.miras.smartclub.service.ReservationService;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

import java.text.SimpleDateFormat;
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
        
        try {
            Event event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
            
            if ("payment_intent.succeeded".equals(event.getType())) {
                PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer()
                    .getObject().orElseThrow();
                
                Map<String, String> metadata = paymentIntent.getMetadata();
                
                Reservation reservation = new Reservation();
                reservation.setClubId(metadata.get("clubId"));
                reservation.setUserId(metadata.get("userId"));
                reservation.setPackageId(metadata.get("packageId"));
                
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                reservation.setStart(sdf.parse(metadata.get("start")));
                reservation.setEnd(sdf.parse(metadata.get("end")));
                
                String seatIdsStr = metadata.get("seatIds");
                List<String> seatIds = Arrays.asList(
                    seatIdsStr.replace("[", "").replace("]", "").split(",\\s*")
                );
                reservation.setSeatIds(seatIds);
                
                reservation.setPaymentIntentId(paymentIntent.getId());
                reservation.setStatus(Reservation.ReservationStatus.ACTIVE);
                
                reservationService.createReservation(reservation);
            }
            
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            return response;
            
        } catch (Exception e) {
            throw new RuntimeException("Webhook error: " + e.getMessage());
        }
    }
}