package com.miras.smartclub.repository;

import com.miras.smartclub.model.Reservation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.Date;
import java.util.List;

public interface ReservationRepository extends MongoRepository<Reservation, String> {

    List<Reservation> findByUserIdOrderByStartDesc(String userId);

    List<Reservation> findByClubId(String clubId);

    @Query("{ 'clubId': ?0, 'seatIds': { $in: ?1 }, 'status': 'ACTIVE', 'start': { $lt: ?3 }, 'end': { $gt: ?2 } }")
    List<Reservation> findActiveOverlapping(String clubId, List<String> seatIds, Date start, Date end);

    @Query("{ 'clubId': ?0, 'status': 'ACTIVE', 'start': { $lt: ?2 }, 'end': { $gt: ?1 } }")
    List<Reservation> findActiveOverlappingAll(String clubId, Date start, Date end);

    // Для очистки старых броней
    List<Reservation> findByEndBefore(Date time);
}
