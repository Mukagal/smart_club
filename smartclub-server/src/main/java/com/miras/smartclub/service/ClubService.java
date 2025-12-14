package com.miras.smartclub.service;

import com.miras.smartclub.model.Club;
import com.miras.smartclub.repository.ClubRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ClubService {
    private final ClubRepository repo;

    public List<Club> findAll() { return repo.findAll(); }
    public Club findById(String id) { return repo.findById(id).orElse(null); }
    public List<Club> saveAll(List<Club> clubs) { return repo.saveAll(clubs); }
    public boolean existsAny() { return repo.count() > 0; }
}
