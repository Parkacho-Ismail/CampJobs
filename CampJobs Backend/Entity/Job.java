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
import java.util.List;

@Entity
@Table(name = "jobs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Job {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long jobId;

    @Column(nullable = false)
    private String jobTitle;

    @Column(nullable = false)
    private String jobType;

    @Column(nullable = false)
    private String jobLocation;

    private Double jobSalary;

    @Column(nullable = false)
    private LocalDateTime expiryDate;

    @Column(nullable = false, length = 3000)
    private String jobDesc;

    @Column(nullable = false, length = 3000)
    private String jobReqs;

    @Column(nullable = false)
    private String jobStatus;

    @Column(nullable = false)
    private LocalDateTime postedAt;

    @ManyToOne
    @JoinColumn(name = "emp_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE) // Ensures jobs are deleted when employer is deleted
    @JsonIgnore // Prevent recursion
    private Employer employer;

    // Add OneToMany relationship with Application entity
    @OneToMany(mappedBy = "job", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnore
    private List<Application> applications;  // Ensure this field exists!



    // Ensure default values before persisting to the database
    @PrePersist
    public void prePersist() {
        if (this.jobStatus == null) {
            this.jobStatus = "NotApproved";
        }
        if (this.postedAt == null) {
            this.postedAt = LocalDateTime.now();
        }
    }
}
