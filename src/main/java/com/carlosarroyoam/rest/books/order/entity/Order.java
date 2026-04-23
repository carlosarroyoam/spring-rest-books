package com.carlosarroyoam.rest.books.order.entity;

import com.carlosarroyoam.rest.books.customer.entity.Customer;
import com.carlosarroyoam.rest.books.payment.entity.Payment;
import com.carlosarroyoam.rest.books.shipment.entity.Shipment;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "order_number", length = 32, nullable = false, unique = true)
  private String orderNumber;

  @Column(name = "notes", length = 1000)
  private String notes;

  @Column(name = "shipping_address", length = 512, nullable = false)
  private String shippingAddress;

  @Column(name = "billing_address", length = 512, nullable = false)
  private String billingAddress;

  @Column(name = "subtotal", precision = 10, scale = 2, nullable = false)
  private BigDecimal subtotal;

  @Column(name = "tax_amount", precision = 10, scale = 2, nullable = false)
  private BigDecimal taxAmount;

  @Column(name = "shipping_amount", precision = 10, scale = 2, nullable = false)
  private BigDecimal shippingAmount;

  @Column(name = "total", precision = 10, scale = 2, nullable = false)
  private BigDecimal total;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", length = 32, nullable = false)
  private OrderStatus status;

  @Builder.Default
  @OneToMany(
      mappedBy = "order",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private List<OrderItem> items = new ArrayList<>();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "customer_id", referencedColumnName = "id", nullable = false)
  private Customer customer;

  @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private Payment payment;

  @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
  private Shipment shipment;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @Column(name = "updated_at", nullable = false)
  private LocalDateTime updatedAt;
}
