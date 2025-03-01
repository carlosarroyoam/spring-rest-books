package com.carlosarroyoam.rest.books.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "books")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Book {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "isbn", length = 17, nullable = false, unique = true)
  private String isbn;

  @Column(name = "title", length = 128, nullable = false)
  private String title;

  @Column(name = "cover_url", length = 512, nullable = false)
  private String coverUrl;

  @Column(name = "price", nullable = false)
  private BigDecimal price;

  @Column(name = "is_available_online", nullable = false)
  private Boolean isAvailableOnline;

  @Builder.Default
  @ManyToMany
  @JoinTable(name = "book_authors", joinColumns = @JoinColumn(name = "book_id"), inverseJoinColumns = @JoinColumn(name = "author_id"))
  private List<Author> authors = new ArrayList<>();

  @Column(name = "published_at", nullable = false)
  private LocalDate publishedAt;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;

  public void addAuthor(Author author) {
    this.authors.add(author);
  }
}
