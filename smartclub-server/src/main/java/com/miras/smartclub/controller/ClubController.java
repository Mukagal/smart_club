package com.miras.smartclub.controller;

import com.miras.smartclub.model.Club;
import com.miras.smartclub.service.ClubService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clubs")
@RequiredArgsConstructor
public class ClubController {

    private final ClubService service;

    @GetMapping
    public ResponseEntity<List<Club>> getAll() {
        return ResponseEntity.ok(service.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable String id) {
        Club club = service.findById(id);
        if (club == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(club);
    }
}
