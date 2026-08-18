package com.andrew.hdss.models;

import com.andrew.hdss.models.enums.Sex;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "individuals")
public class Individual extends SyncableEntity {

    @Column(nullable = false, unique = true)
    private String extendedId;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Sex sex;

    @Column(nullable = false)
    private LocalDate dateOfBirth;

    @Column(nullable = false)
    private boolean dobEstimated = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mother_id")
    private Individual mother;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "father_id")
    private Individual father;
}