package com.example.campsjobs.Repository;
import org.springframework.transaction.annotation.Transactional;
import com.example.campsjobs.Entity.Employer;
import com.example.campsjobs.Entity.Job;
import com.example.campsjobs.Entity.JobSeeker;
import com.example.campsjobs.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    List<Job> findByEmployer(Employer employer);  // Find jobs by Employer entity
    List<Job> findByEmployer_EmpId(Long empId);  // Find jobs by employer's empId (Long)

    @Query("SELECT j FROM Job j JOIN FETCH j.employer")
    List<Job> findAllWithEmployers();

    //Employer to find their job-posts
//    List<Job> findByEmployerUserId(Long userId);
    List<Job> findByEmployerUserUserId(Long userId);


    @Modifying
    @Transactional
    @Query("UPDATE Job j SET j.jobStatus = :status WHERE j.jobId = :jobId")
    void updateJobStatus(@Param("jobId") Long jobId, @Param("status") String status);

    @Modifying
    @Transactional
    @Query("DELETE FROM Job j WHERE j.jobId = :jobId")
    void deleteJobById(@Param("jobId") Long jobId);

    @Modifying
    @Transactional
    @Query("UPDATE Job j SET j.jobStatus = 'Expired' WHERE j.expiryDate < CURRENT_DATE AND j.jobStatus <> 'Expired'")
    void markExpiredJobs();

    @Query("SELECT COUNT(a) FROM Application a WHERE a.job.jobId = :jobId")
    int countApplicationsByJobId(Long jobId);

    //applications to a job
//    Optional<Job> findByIdAndEmployer(Long jobId, Users employer);


//    Optional<Job> findByIdAndEmployer_User_UserId(Long jobId, Long userId);
//    Optional<Job> findByIdAndEmployer_User(Long jobId, Users employerUser);
//    Optional<Job> findByIdAndEmployer(Long jobId, Employer employer);

    List<Job> findByEmployer_User(Users user);

    Optional<Job> findByJobIdAndEmployer_EmpId(Long jobId, Long empId);



//    Optional<Job> findByIdAndEmployer(Long jobId, Employer employer);

    @Transactional
    @Modifying
    @Query("DELETE FROM Job j WHERE j.jobId = :jobId AND j.employer.empId = :empId")
    int deleteByJobIdAndEmployer_EmpId(@Param("jobId") Long jobId, @Param("empId") Long empId);


    Optional<Job> findByJobId(Long jobId);

    @Query("SELECT j.jobTitle, e.empName, COUNT(a) " +
            "FROM Job j " +
            "JOIN j.employer e " +
            "LEFT JOIN Application a ON j.jobId = a.job.jobId " +
            "GROUP BY j.jobId, j.jobTitle, e.empName " +
            "ORDER BY COUNT(a) DESC")
    List<Object[]> findTopJobsByApplications();


    long countByPostedAtAfter(LocalDateTime postedAt);

    long countByPostedAtBetween(LocalDateTime startDate, LocalDateTime endDate);


}
//public interface JobRepository extends JpaRepository<Job, Long> {
//    //If you want to find all employers, the method should be:
//    List<Job> findAll();
//    Optional<Job> findById(Long id);
//    List<Job> findByEmployer(Long employer);
////    List<Job> findByEmployerEmpId(Long empId);
//}
