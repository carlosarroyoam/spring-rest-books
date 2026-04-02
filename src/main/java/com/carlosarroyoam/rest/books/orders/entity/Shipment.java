package com.carlosarroyoam.rest.books.orders.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "shipments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipment {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "attention_name", length = 128)
  private String attentionName;

  @Column(name = "address", length = 512, nullable = false)
  private String address;

  @Column(name = "phone", length = 32)
  private String phone;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", length = 32, nullable = false)
  private ShipmentStatus status;

  @Column(name = "order_id", nullable = false, unique = true)
  private Long orderId;

  @OneToOne
  @JoinColumn(name = "order_id", referencedColumnName = "id", insertable = false, updatable = false, nullable = false)
  private Order order;
}
