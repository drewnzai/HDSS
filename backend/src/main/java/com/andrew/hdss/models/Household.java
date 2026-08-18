package com.andrew.hdss.models;

import com.andrew.hdss.models.enums.HouseholdStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "households")
public class Household extends SyncableEntity {

    @Column(nullable = false, unique = true)
    private String householdCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    private Double latitude;
    private Double longitude;

    // Nullable by design: a null head IS the NOT_VIABLE case. `unique = true`
    // stops the same individual being recorded as head of two households.
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "head_id", unique = true)
    private Individual head;

    // Defaults to NOT_VIABLE — a household starts headless until a
    // Membership with relationshipToHead = HEAD is created for it.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private HouseholdStatus status = HouseholdStatus.NOT_VIABLE;
}