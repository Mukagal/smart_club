package com.miras.smartclub.controller;

import com.miras.smartclub.model.Club;
import com.miras.smartclub.model.Reservation;
import com.miras.smartclub.service.ClubService;
import com.miras.smartclub.service.ReservationService;
import jakarta.servlet.http.HttpSession;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.regex.Pattern;

/**
 * Контроллер бронирований — улучшенная версия с вычислением totalPrice на сервере
 * и диагностическим логированием для отладки.
 */
@RestController
@RequestMapping("/booking")
@RequiredArgsConstructor
public class BookingController {

    private final ReservationService reservationService;
    private final ClubService clubService;

    @Data
    public static class AvailabilityRequest {
        private String clubId;
        private List<String> seatIds; // optional
        private Date start;
        private Date end;
    }

    @Data
    public static class ReserveRequest {
        private String clubId;
        private List<String> seatIds;
        private Date start;
        private Date end;
        private Integer durationMinutes;
        private String packageId;
        private Integer totalPrice;
    }

    @Data
    public static class CancelRequest {
        private String reservationId;
    }

    @PostMapping("/availability")
    public ResponseEntity<?> availability(@RequestBody AvailabilityRequest req) {
        if (req.getClubId() == null || req.getStart() == null || req.getEnd() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "clubId, start and end required"));
        }

        if (req.getSeatIds() == null || req.getSeatIds().isEmpty()) {
            Map<String, Object> avail = reservationService.getAvailability(req.getClubId(), req.getStart(), req.getEnd());
            return ResponseEntity.ok(avail);
        }

        List<Reservation> conflicts = reservationService.findConflicts(req.getClubId(), req.getSeatIds(), req.getStart(), req.getEnd());
        return ResponseEntity.ok(Map.of("conflicts", conflicts));
    }

    @PostMapping("/reserve")
    public ResponseEntity<?> reserve(@RequestBody ReserveRequest req, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));

        if (req.getClubId() == null || req.getSeatIds() == null || req.getSeatIds().isEmpty() || req.getStart() == null || req.getEnd() == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "clubId, seatIds, start, end required"));
        }

        // Check conflicts
        List<Reservation> conflicts = reservationService.findConflicts(req.getClubId(), req.getSeatIds(), req.getStart(), req.getEnd());
        if (!conflicts.isEmpty()) {
            return ResponseEntity.status(409).body(Map.of("error", "Some seats already booked", "conflicts", conflicts));
        }

        // Build reservation
        Reservation r = new Reservation();
        r.setClubId(req.getClubId());
        r.setSeatIds(req.getSeatIds());
        r.setStart(req.getStart());
        r.setEnd(req.getEnd());
        r.setDurationMinutes(req.getDurationMinutes());
        r.setPackageId(req.getPackageId());
        r.setUserId(userId);
        r.setStatus(Reservation.ReservationStatus.PENDING); // ADD THIS LINE - default to PENDING until payment

        // Compute price: prefer provided, otherwise compute on server
        Integer provided = req.getTotalPrice();
        Integer computed = null;
        if (provided != null) {
            computed = provided;
        } else {
            computed = computePriceForReservation(req.getClubId(), req.getPackageId(), req.getSeatIds() == null ? 0 : req.getSeatIds().size());
        }
        r.setTotalPrice(computed);

        Reservation saved = reservationService.createReservation(r);

        Map<String, Object> resp = new HashMap<>();
        resp.put("message", "Reservation created");
        resp.put("reservationId", saved.getId());
        resp.put("reservation", saved);
        resp.put("computedPrice", computed); // полезно для отладки на клиенте

        // diagnostic logs (temporarily)
        System.out.println("[BOOKING] reserve: clubId=" + req.getClubId() + " packageId=" + req.getPackageId() + " seats=" + req.getSeatIds() + " provided=" + provided + " computed=" + computed);

        return ResponseEntity.ok(resp);
    }

    @PostMapping("/cancel")
    public ResponseEntity<?> cancel(@RequestBody CancelRequest req, HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        if (req.getReservationId() == null) return ResponseEntity.badRequest().body(Map.of("error", "reservationId required"));

        Optional<Reservation> maybe = reservationService.getById(req.getReservationId());
        if (maybe.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Reservation not found"));

        Reservation found = maybe.get();
        if (!userId.equals(found.getUserId())) return ResponseEntity.status(403).body(Map.of("error", "Not owner"));

        if (found.getStatus() == Reservation.ReservationStatus.CANCELLED) {
            return ResponseEntity.badRequest().body(Map.of("error", "Already cancelled"));
        }

        Optional<Reservation> cancelled = reservationService.cancelReservation(req.getReservationId(), userId);
        if (cancelled.isPresent()) {
            return ResponseEntity.ok(Map.of("message", "Cancelled", "reservationId", req.getReservationId()));
        } else {
            return ResponseEntity.status(500).body(Map.of("error", "Cancellation failed"));
        }
    }

    @GetMapping("/history")
    public ResponseEntity<?> history(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        List<Reservation> history = reservationService.getUserHistory(userId);
        return ResponseEntity.ok(Map.of("history", history));
    }

    @PostMapping("/clear-past")
    public ResponseEntity<?> clearPast(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body(Map.of("error", "Not authenticated"));
        }
        try {
            int removed = reservationService.clearPastReservationsForUser(userId);
            return ResponseEntity.ok(Map.of("removed", removed));
        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }

    // --------------------
    // Helper methods
    // --------------------

    private static final Pattern NON_DIGIT = Pattern.compile("[^0-9]");

    /**
     * Compute price: try to find matching PriceItem in club.prices and return singlePrice * seatCount.
     * Robust matching: normalize strings, compare by several fields, partial contains.
     */
    private Integer computePriceForReservation(String clubId, String packageId, int seatCount) {
        if (clubId == null || packageId == null || seatCount <= 0) return null;

        try {
            Club club = clubService.findById(clubId);
            if (club == null) {
                System.out.println("[BOOKING] computePrice - club not found: " + clubId);
                return null;
            }
            if (club.getPrices() == null || club.getPrices().isEmpty()) {
                System.out.println("[BOOKING] computePrice - no prices for club: " + clubId);
                return null;
            }

            String normPackage = normalizeKey(packageId);

            // Build candidate list: normalize price items with multiple text keys
            List<Club.PriceItem> items = club.getPrices();

            // 1) exact matches on service/category/type/title (normalized)
            for (Club.PriceItem pi : items) {
                if (pi == null) continue;
                List<String> keys = Arrays.asList(pi.getService(), pi.getCategory(), pi.getType(), pi.getResourceType());
                for (String k : keys) {
                    if (k == null) continue;
                    if (normalizeKey(k).equals(normPackage)) {
                        Integer single = extractPriceNumber(pi);
                        if (single != null) {
                            System.out.println("[BOOKING] computePrice - exact match on key='" + k + "' price=" + single);
                            return single * seatCount;
                        }
                    }
                }
            }

            // 2) exact match on numeric id if packageId looks like an id (many setups use id or code)
            // (If price items had an 'id' field, we'd compare here — but model doesn't have it.)

            // 3) partial contains (if packageId is substring of service/category or vice-versa)
            for (Club.PriceItem pi : items) {
                if (pi == null) continue;
                String combined = (pi.getService() == null ? "" : pi.getService()) + " " + (pi.getCategory() == null ? "" : pi.getCategory()) + " " + (pi.getType() == null ? "" : pi.getType());
                String normCombined = normalizeKey(combined);
                if (normCombined.contains(normPackage) || normPackage.contains(normCombined)) {
                    Integer single = extractPriceNumber(pi);
                    if (single != null) {
                        System.out.println("[BOOKING] computePrice - partial match on combined='" + combined + "' price=" + single);
                        return single * seatCount;
                    }
                }
            }

            // 4) try to pick the first bookable price item that has a number (fallback)
            for (Club.PriceItem pi : items) {
                if (pi == null) continue;
                if (Boolean.TRUE.equals(pi.getBookable()) || true) { // allow fallback
                    Integer single = extractPriceNumber(pi);
                    if (single != null) {
                        System.out.println("[BOOKING] computePrice - fallback to first priceItem price=" + single);
                        return single * seatCount;
                    }
                }
            }

            // nothing found
            System.out.println("[BOOKING] computePrice - nothing matched for packageId='" + packageId + "' in club " + clubId);
            return null;
        } catch (Exception e) {
            System.out.println("[BOOKING] computePrice exception: " + e.getMessage());
            return null;
        }
    }

    private String normalizeKey(String s) {
        if (s == null) return "";
        String t = s.trim().toLowerCase();
        // remove punctuation characters that could break matching
        t = t.replaceAll("[\\p{Punct}]+", " ");
        t = t.replaceAll("\\s+", " ");
        return t;
    }

    private Integer extractPriceNumber(Club.PriceItem pi) {
        if (pi == null) return null;
        if (pi.getPriceNumber() != null) return pi.getPriceNumber();
        if (pi.getPrice() != null && !pi.getPrice().isBlank()) {
            String digits = NON_DIGIT.matcher(pi.getPrice()).replaceAll("");
            if (digits != null && !digits.isBlank()) {
                try {
                    return Integer.parseInt(digits);
                } catch (NumberFormatException ex) {
                    return null;
                }
            }
        }
        return null;
    }
}
