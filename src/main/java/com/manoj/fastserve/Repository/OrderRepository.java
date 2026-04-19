package com.manoj.fastserve.Repository;

import com.manoj.fastserve.Entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}