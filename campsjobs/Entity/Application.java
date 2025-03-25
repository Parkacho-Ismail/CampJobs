package com.example.campsjobs.Entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "applications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Application {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long appId;

    @ManyToOne
    @JoinColumn(name = "job_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE) // Ensures deletion when job is deleted
    private Job job;

    @ManyToOne
    @JoinColumn(name = "seeker_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE) // Ensures deletion when job seeker is deleted
    @JsonIgnore // Prevents recursive nesting
    private Users jobSeeker; // Job seeker linked to Users entity

    private String jobSeekerEmail;

    private String resumeImg;
    private String letterImg;
    private String certImg;
    private String idImg;

    @Column(nullable = false)
    private LocalDateTime appliedAt = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApplicationStatus appStatus = ApplicationStatus.PENDING;

    public enum ApplicationStatus {
        PENDING,
        ACCEPTED,
        SHORTLISTED,
        REJECTED
    }
}
