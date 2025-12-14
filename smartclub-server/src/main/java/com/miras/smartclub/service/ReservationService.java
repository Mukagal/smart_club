package com.miras.smartclub.service;

import com.miras.smartclub.model.Reservation;
import com.miras.smartclub.model.Seat;
import com.miras.smartclub.repository.ReservationRepository;
import com.miras.smartclub.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final SeatRepository seatRepository;

    public List<Reservation> findConflicts(String clubId, List<String> seatIds, Date start, Date end) {
        if (seatIds == null || seatIds.isEmpty()) return Collections.emptyList();
        return reservationRepository.findActiveOverlapping(clubId, seatIds, start, end);
    }

    @Transactional
    public Reservation createReservation(Reservation r) {
        if (r == null) throw new IllegalArgumentException("Reservation is null");
        if (r.getCreatedAt() == null) r.setCreatedAt(new Date());
        if (r.getStatus() == null) r.setStatus(Reservation.ReservationStatus.ACTIVE);
        return reservationRepository.save(r);
    }

    @Transactional
    public Optional<Reservation> cancelReservation(String reservationId, String cancelledByUserId) {
        Optional<Reservation> maybe = reservationRepository.findById(reservationId);
        if (maybe.isEmpty()) return Optional.empty();

        Reservation r = maybe.get();
        if (r.getStatus() == Reservation.ReservationStatus.CANCELLED) {
            return Optional.of(r);
        }

        r.setStatus(Reservation.ReservationStatus.CANCELLED);
        r.setCancelledAt(new Date());
        r.setCancelledBy(cancelledByUserId);
        Reservation saved = reservationRepository.save(r);
        return Optional.of(saved);
    }

    public List<Reservation> getUserHistory(String userId) {
        return reservationRepository.findByUserIdOrderByStartDesc(userId);
    }

    public Optional<Reservation> getById(String id) {
        return reservationRepository.findById(id);
    }

    public Map<String, Object> getAvailability(String clubId, Date start, Date end) {
        List<Seat> seats = seatRepository.findByClubIdOrderByOrderAsc(clubId);
        List<String> seatIds = seats.stream().map(Seat::getId).collect(Collectors.toList());

        List<Reservation> overlapping = reservationRepository.findActiveOverlapping(clubId, seatIds, start, end);

        Set<String> occupied = new HashSet<>();
        for (Reservation r : overlapping) {
            if (r.getSeatIds() != null) occupied.addAll(r.getSeatIds());
        }

        List<Map<String, Object>> seatsDto = seats.stream().map(s -> {
            Map<String, Object> m = new HashMap<>();
            m.put("id", s.getId());
            m.put("label", s.getLabel());
            m.put("isVip", s.isVip());
            m.put("order", s.getOrder());
            m.put("available", !occupied.contains(s.getId()));
            return m;
        }).collect(Collectors.toList());

        long total = seats.size();
        long vip = seats.stream().filter(Seat::isVip).count();
        long availableCount = seatsDto.stream().filter(s -> (Boolean) s.get("available")).count();
        long availableVip = seatsDto.stream().filter(s -> (Boolean) s.get("available") && (Boolean) s.get("isVip")).count();

        Map<String, Object> result = new HashMap<>();
        result.put("start", start);
        result.put("end", end);
        result.put("totalSeats", total);
        result.put("vipSeats", vip);
        result.put("availableCount", availableCount);
        result.put("availableVipCount", availableVip);
        result.put("seats", seatsDto);

        return result;
    }

    /**
     * Удалить физически все прошедшие резервации (end < now). Возвращает количество удалённых.
     * (Старый метод для глобальной очистки, если нужен где-то ещё)
     */
    @Transactional
    public int clearPastReservations() {
        Date now = new Date();
        List<Reservation> past = reservationRepository.findByEndBefore(now);
        if (past == null || past.isEmpty()) return 0;
        reservationRepository.deleteAll(past);
        return past.size();
    }

    /**
     * Удалить прошедшие (end < now) и отменённые брони **для конкретного пользователя**.
     * Возвращает количество удалённых записей.
     */
    @Transactional
    public int clearPastReservationsForUser(String userId) {
        if (userId == null) return 0;
        Date now = new Date();
        List<Reservation> all = reservationRepository.findByUserIdOrderByStartDesc(userId);
        if (all == null || all.isEmpty()) return 0;

        List<Reservation> toDelete = new ArrayList<>();
        for (Reservation r : all) {
            boolean isCancelled = r.getStatus() == Reservation.ReservationStatus.CANCELLED;
            boolean isPast = r.getEnd() != null && r.getEnd().before(now);
            if (isCancelled || isPast) {
                toDelete.add(r);
            }
        }

        if (toDelete.isEmpty()) return 0;
        reservationRepository.deleteAll(toDelete);
        return toDelete.size();
    }

    /**
     * Возвращает активные брони пользователя (end > now)
     */
    public List<Reservation> getActiveReservations(String userId) {
        Date now = new Date();
        List<Reservation> all = reservationRepository.findByUserIdOrderByStartDesc(userId);
        return all.stream()
                .filter(r -> r.getStatus() == Reservation.ReservationStatus.ACTIVE && r.getEnd() != null && r.getEnd().after(now))
                .collect(Collectors.toList());
    }

    /**
     * Возвращает прошедшие брони пользователя (end <= now OR CANCELLED)
     */
    public List<Reservation> getPastReservations(String userId) {
        Date now = new Date();
        List<Reservation> all = reservationRepository.findByUserIdOrderByStartDesc(userId);
        return all.stream()
                .filter(r -> r.getStatus() == Reservation.ReservationStatus.CANCELLED || r.getEnd() == null || !r.getEnd().after(now))
                .collect(Collectors.toList());
    }
}
