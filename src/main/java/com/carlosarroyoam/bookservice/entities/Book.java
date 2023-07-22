package com.carlosarroyoam.bookservice.entities;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 128, nullable = false)
    private String title;

    @Column(length = 128, nullable = false)
    private String author;

    @Column(nullable = false)
    private Double price;

    @Column(nullable = false)
    private LocalDate publishedAt;

    @Column(nullable = false)
    private boolean isAvailableOnline;

    public Book(String title, String author, Double price, LocalDate publishedAt, boolean isAvailableOnline) {
        super();
        this.title = title;
        this.author = author;
        this.price = price;
        this.publishedAt = publishedAt;
        this.isAvailableOnline = isAvailableOnline;
    }
}
