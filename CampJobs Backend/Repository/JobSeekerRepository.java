package com.example.campsjobs.Repository;

import com.example.campsjobs.Entity.Job;
import com.example.campsjobs.Entity.JobSeeker;
import com.example.campsjobs.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface JobSeekerRepository extends JpaRepository<JobSeeker, Long>{
    Optional<JobSeeker> findByseekerId(Long seekerId);
    Optional<JobSeeker> findByUser(Users user);

    Optional<JobSeeker> findByUser_UserId(Long userId); // Find job seeker by user ID

    @Query("SELECT s FROM JobSeeker s WHERE s.user.role = 'JOBSEEKER'")
    List<JobSeeker> findAll();

    @Query("SELECT s FROM JobSeeker s WHERE s.user.role = 'JOBSEEKER'")
    List<JobSeeker> findAllJobSeekers();

    @Query("SELECT js.seekerId FROM JobSeeker js WHERE js.user.userId = :userId")
    Long findSeekerIdByUserId(@Param("userId") Long userId);

    Optional<JobSeeker> findByUserUserId(Long userId); // Find job seeker by userId

}

