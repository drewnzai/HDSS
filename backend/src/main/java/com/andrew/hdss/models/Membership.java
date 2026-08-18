package com.andrew.hdss.models;

import com.andrew.hdss.models.enums.MembershipEndType;
import com.andrew.hdss.models.enums.MembershipStartType;
import com.andrew.hdss.models.enums.RelationshipToHead;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "memberships")
public class Membership extends SyncableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "individual_id", nullable = false)
    private Individual individual;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "household_id", nullable = false)
    private Household household;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RelationshipToHead relationshipToHead;

    @Column(nullable = false)
    private LocalDate startDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipStartType startType;

    // Null = this is the individual's current, open membership.
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    private MembershipEndType endType;
}