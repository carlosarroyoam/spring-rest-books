package com.carlosarroyoam.rest.books.orders;

import com.carlosarroyoam.rest.books.orders.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
