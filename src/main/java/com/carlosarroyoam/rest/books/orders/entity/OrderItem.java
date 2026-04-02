package com.carlosarroyoam.rest.books.orders.entity;

import com.carlosarroyoam.rest.books.book.entity.Book;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "order_items")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "quantity", nullable = false)
  private Integer quantity;

  @Column(name = "unit_price", precision = 10, scale = 2, nullable = false)
  private BigDecimal unitPrice;

  @Column(name = "total_price", precision = 10, scale = 2, nullable = false)
  private BigDecimal totalPrice;

  @Column(name = "book_id", nullable = false)
  private Long bookId;

  @ManyToOne
  @JoinColumn(name = "book_id", referencedColumnName = "id", insertable = false, updatable = false, nullable = false)
  private Book book;

  @Column(name = "order_id", nullable = false, insertable = false, updatable = false)
  private Long orderId;

  @ManyToOne
  @JoinColumn(name = "order_id", referencedColumnName = "id", nullable = false)
  private Order order;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}
