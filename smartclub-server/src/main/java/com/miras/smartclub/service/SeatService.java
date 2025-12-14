package com.miras.smartclub.service;

import com.miras.smartclub.model.Seat;
import com.miras.smartclub.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SeatService {
    private final SeatRepository seatRepository;

    public List<Seat> getSeatsByClub(String clubId) {
        return seatRepository.findByClubIdOrderByOrderAsc(clubId);
    }
}
