package com.andrew.hdss.models;// package: match your existing entity package
// Update to the Form given earlier — only change is the new `status` field.

import com.andrew.hdss.models.enums.FormCategory;
import com.andrew.hdss.models.enums.FormStatus;
import com.andrew.hdss.models.enums.FormTarget;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "forms")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Form {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormCategory category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormTarget target;

    @Column(nullable = false)
    private Integer version;

    private String description;

    @Column(nullable = false)
    private boolean active;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormStatus status;

    @OneToMany(mappedBy = "form", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    @Builder.Default
    private List<Question> questions = new ArrayList<>();

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
