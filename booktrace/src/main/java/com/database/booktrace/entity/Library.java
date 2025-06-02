package com.database.booktrace.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "libraries")
@Getter
@Setter
public class Library {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "library_seq")
    @SequenceGenerator(name = "library_seq", sequenceName = "library_seq", allocationSize = 1)
    @Column(name = "library_id")
    private Long libraryId;

    @Column(nullable = false)
    private String name;
} 