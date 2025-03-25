package com.example.campsjobs.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDate;

@Entity
@Table(name = "jobseeker")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class JobSeeker {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long seekerId;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "userId", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Users user;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Column(nullable = false)
    private String seekerGender;

    @Column(nullable = false)
    private String seekerNat;

    @Column(nullable = false)
    private String seekerTel;

    @Column(nullable = false)
    private LocalDate seekerDob;

    @Column(nullable = true)
    private String seekerImg;
}
