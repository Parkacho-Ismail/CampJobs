package com.example.campsjobs.Repository;


import com.example.campsjobs.Entity.Employer;
import com.example.campsjobs.Entity.JobSeeker;
import com.example.campsjobs.Entity.Users;
import org.apache.catalina.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployerRepository extends JpaRepository<Employer, Long> {
    //to fetch a specific employer.
    Optional<Employer> findByempId(Long seekerId);
    //If you want to find all employers, the method should be:
    List<Employer> findAll();

    Optional<Employer> findByUser(Users user);
    Optional<Employer> findByUserUserId(Long userId);

    Optional<Employer> findByUser_Email(String email);  // Use the email from the User table

    Optional<Employer> findByUser_UserId(Long userId); // Fix: Correct method signature

}
