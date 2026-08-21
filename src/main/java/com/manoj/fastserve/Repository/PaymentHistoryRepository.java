package com.manoj.fastserve.Repository;

import com.manoj.fastserve.Entity.PaymentHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentHistoryRepository
        extends JpaRepository<PaymentHistory, Long> {

    List<PaymentHistory> findByOrderIdOrderByCreatedAtDesc(Long orderId);

}