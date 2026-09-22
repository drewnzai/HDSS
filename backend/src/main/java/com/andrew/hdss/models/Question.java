package com.andrew.hdss.models;

import com.andrew.hdss.models.enums.MappedEntity;
import com.andrew.hdss.models.enums.QuestionType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "questions")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "form_id", nullable = false)
    private Form form;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String label;

    private String hint;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType type;

    @Column(nullable = false)
    private boolean required;

    private String relevant;
    private String constraint;
    private String constraintMessage;
    private String calculation;
    private String choiceListName;

    @Column(nullable = false)
    private Integer orderIndex;

    // Which core entity (if any) this question's answer feeds into once
    // translated on-device — Android reads this to drive local
    // Household/Individual/Membership creation for Core forms. NONE for
    // ordinary Extra-form questions that just stay as generic Answers.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private MappedEntity mappedEntity = MappedEntity.NONE;

    // e.g. "firstName", "latitude", "relationshipToHead" — the target
    // field name on whatever mappedEntity points to. Null when
    // mappedEntity is NONE.
    private String mappedField;
}
