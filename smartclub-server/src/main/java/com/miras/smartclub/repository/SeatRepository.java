package com.miras.smartclub.repository;

import com.miras.smartclub.model.Seat;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SeatRepository extends MongoRepository<Seat, String> {
    List<Seat> findByClubIdOrderByOrderAsc(String clubId);
    long countByClubId(String clubId);
}
