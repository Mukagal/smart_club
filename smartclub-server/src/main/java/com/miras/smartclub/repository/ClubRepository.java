package com.miras.smartclub.repository;

import com.miras.smartclub.model.Club;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClubRepository extends MongoRepository<Club, String> {
}
