package com.carlosarroyoam.rest.books.entities;

import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "authors")
@Data
@NoArgsConstructor
public class Author {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(name = "first_name", length = 64, nullable = false)
	private String firstName;

	@Column(name = "last_name", length = 64, nullable = false)
	private String lastName;

	@ManyToMany(mappedBy = "authors", fetch = FetchType.LAZY)
	private List<Book> books;

	@Column(name = "created_at", nullable = false)
	private LocalDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private LocalDateTime updatedAt;

	public Author(String firstName, String lastName, LocalDateTime createdAt, LocalDateTime updatedAt) {
		this.firstName = firstName;
		this.lastName = lastName;
		this.createdAt = createdAt;
		this.updatedAt = updatedAt;
	}

}
