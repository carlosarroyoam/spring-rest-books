package com.carlosarroyoam.rest.books.entities;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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

	@Column(name = "title", length = 128, nullable = false)
	private String title;

	@Column(name = "price", nullable = false)
	private BigDecimal price;

	@ManyToMany(fetch = FetchType.EAGER)
	@JoinTable(name = "book_authors", joinColumns = @JoinColumn(name = "book_id"), inverseJoinColumns = @JoinColumn(name = "author_id"))
	private List<Author> authors = new ArrayList<>();

	@Column(name = "published_at", nullable = false)
	private LocalDate publishedAt;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	@Column(name = "is_available_online", nullable = false)
	private boolean isAvailableOnline;

	public Book(String title, BigDecimal price, LocalDate publishedAt, LocalDateTime createdAt, LocalDateTime updatedAt,
			boolean isAvailableOnline) {
		this.title = title;
		this.price = price;
		this.publishedAt = publishedAt;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
		this.isAvailableOnline = isAvailableOnline;
	}

	public void addAuthor(Author author) {
		this.authors.add(author);
	}

}
