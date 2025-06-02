package com.database.booktrace.Domain;

import jakarta.persistence.*;
import lombok.*;
import java.util.List;

// Library 엔티티 (전자도서관)
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "LIBRARIES")
@SequenceGenerator(
        name = "library_seq_gen",
        sequenceName = "seq_library_id",
        initialValue = 1,
        allocationSize = 1
)
public class Library extends BaseEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "library_seq_gen")
    @Column(name = "LIBRARY_ID")
    private Long libraryId;

    @Column(name = "NAME", nullable = false, length = 100)
    private String name;

    @OneToMany(mappedBy = "library", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Book> books;

}