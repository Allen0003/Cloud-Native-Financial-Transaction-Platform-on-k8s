package com.transaction.domain.repository;

import com.transaction.domain.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


@Repository
public interface UserRepository extends MongoRepository<User, String> {

    // Custom query: Find user by the Shard Key
    // Finding by userId is efficient in a sharded cluster (Targeted Query)
    Optional<User> findByUserId(String userId);
}
