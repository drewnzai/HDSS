package com.andrew.hdss.models;

import com.andrew.hdss.models.enums.RoundStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "rounds")
@Getter
@Setter
public class Round {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "round_name", nullable = false, length = 100)
    private String roundName;

    @Column(name = "database_name", nullable = false, unique = true, length = 128)
    private String databaseName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RoundStatus status;

    @Column(name = "validation_passed", nullable = false)
    private boolean validationPassed;

    @Column(name = "validation_report", columnDefinition = "JSON")
    private String validationReport;

    @Column(name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "created_by", nullable = false)
    private User createdBy;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        LocalDateTime now = LocalDateTime.now();

        if (createdAt == null) {
            createdAt = now;
        }

        updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}