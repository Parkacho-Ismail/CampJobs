package com.example.campsjobs.Repository;

import com.example.campsjobs.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<Users, Long> {
    Optional<Users> findByEmail(String email);

    Optional<Users> findByUserId(Long userId);

    List<Users> findByCreatedAtAfter(LocalDateTime createdAt);

    long countByCreatedAtAfter(LocalDate createdAt);

    long countByCreatedAtBetween(LocalDateTime  startDate, LocalDateTime  endDate);

}
