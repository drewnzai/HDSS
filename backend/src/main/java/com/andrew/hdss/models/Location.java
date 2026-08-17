package com.andrew.hdss.models;

import com.andrew.hdss.models.enums.LocationType;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "locations",
        uniqueConstraints = @UniqueConstraint(columnNames = {"parent_id", "name"})
)
@Data
@NoArgsConstructor
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LocationType type;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Location parent;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<Location> children = new ArrayList<>();

    // e.g. "1/5/23/" — ids of every ancestor, root to immediate parent, trailing slash included.
    // Lets "all descendants of X" resolve to: WHERE ancestor_path LIKE '1/5/23/%'
    @Column(name = "ancestor_path", nullable = false)
    private String ancestorPath;

    // Optional government/statistical code (e.g. KNBS codes), useful for imports/reporting
    @Column(unique = true)
    private String code;
}