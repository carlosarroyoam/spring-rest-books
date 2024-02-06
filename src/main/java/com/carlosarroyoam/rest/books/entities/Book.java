package com.carlosarroyoam.rest.books.entities;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
public class Book {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 128, nullable = false)
	private String title;

	@Column(length = 128, nullable = false)
	private String author;

	@Column(nullable = false)
	private BigDecimal price;

	@Column(nullable = false)
	private LocalDate publishedAt;

	@Column(nullable = false)
	private boolean isAvailableOnline;

	public Book(String title, String author, BigDecimal price, LocalDate publishedAt, boolean isAvailableOnline) {
		this.title = title;
		this.author = author;
		this.price = price;
		this.publishedAt = publishedAt;
		this.isAvailableOnline = isAvailableOnline;
	}

}
