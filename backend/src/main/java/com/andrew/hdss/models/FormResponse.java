package com.andrew.hdss.models;

import com.andrew.hdss.models.enums.FormResponseStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "form_responses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FormResponse extends SyncableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visit_id", nullable = false)
    private Visit visit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id", nullable = false)
    private Form form;

    @Column(nullable = false)
    private Integer formVersion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private FormResponseStatus status;
}
