package com.manoj.fastserve.Repository;

import com.manoj.fastserve.Entity.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@MockitoBean(types = CacheManager.class)
class PaymentHistoryRepositoryTest {

    @Autowired
    private PaymentHistoryRepository paymentHistoryRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    private Order createOrder() {

        User user = new User();
        user.setName("Manoj");
        user.setEmail("history@test.com");
        user.setPassword("password");
        user.setAddress("India");
        user.setRole(Role.USER);

        user = userRepository.save(user);

        Order order = new Order();
        order.setUser(user);
        order.setTotalPrice(500.0);
        order.setStatus(OrderStatus.PLACED);
        order.setPaymentMode(PaymentMode.UPI);
        order.setPaid(false);

        return orderRepository.save(order);
    }

    @Test
    void findByOrderIdOrderByCreatedAtDesc_shouldReturnHistory() {

        Order order = createOrder();

        PaymentHistory first = new PaymentHistory();
        first.setOrder(order);
        first.setPaymentMode(PaymentMode.UPI);
        first.setPaymentStatus(PaymentStatus.FAILED);
        first.setAmount(500.0);
        first.setMessage("Failed");

        paymentHistoryRepository.save(first);

        PaymentHistory second = new PaymentHistory();
        second.setOrder(order);
        second.setPaymentMode(PaymentMode.UPI);
        second.setPaymentStatus(PaymentStatus.SUCCESS);
        second.setTransactionId("TXN123");
        second.setAmount(500.0);
        second.setMessage("Success");

        paymentHistoryRepository.save(second);

        List<PaymentHistory> history =
                paymentHistoryRepository.findByOrderIdOrderByCreatedAtDesc(order.getId());

        assertThat(history).hasSize(2);
        assertThat(history.get(0).getOrder().getId()).isEqualTo(order.getId());
    }
}