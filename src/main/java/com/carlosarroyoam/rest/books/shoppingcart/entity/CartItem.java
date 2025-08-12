package com.carlosarroyoam.rest.books.shoppingcart.entity;

import com.carlosarroyoam.rest.books.book.entity.Book;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cart_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "quantity", nullable = false)
  private Integer quantity;

  @Column(name = "book_id", nullable = false)
  private Long bookId;

  @ManyToOne
  @JoinColumn(name = "book_id", referencedColumnName = "id", insertable = false, updatable = false, nullable = false)
  private Book book;

  @Column(name = "shopping_cart_id", nullable = false)
  private Long shoppingCartId;

  @ManyToOne
  @JoinColumn(name = "shopping_cart_id", referencedColumnName = "id", insertable = false, updatable = false, nullable = false)
  private ShoppingCart shoppingCart;

  @Column(name = "added_at", nullable = false)
  private LocalDateTime addedAt;
}
