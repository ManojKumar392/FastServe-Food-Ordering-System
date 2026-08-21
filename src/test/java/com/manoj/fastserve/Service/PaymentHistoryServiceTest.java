package com.manoj.fastserve.Service;

import com.manoj.fastserve.DTO.PaymentHistoryResponseDTO;
import com.manoj.fastserve.Entity.*;
import com.manoj.fastserve.Exception.UnauthorizedException;
import com.manoj.fastserve.Repository.OrderRepository;
import com.manoj.fastserve.Repository.PaymentHistoryRepository;
import com.manoj.fastserve.Repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentHistoryServiceTest {

    @Mock
    private PaymentHistoryRepository paymentHistoryRepository;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private PaymentHistoryService paymentHistoryService;

    private User user;
    private User admin;
    private Order order;

    @BeforeEach
    void setup() {

        user = new User();
        user.setId(1L);
        user.setName("Manoj");
        user.setEmail("manoj@test.com");
        user.setRole(Role.USER);

        admin = new User();
        admin.setId(2L);
        admin.setName("Admin");
        admin.setEmail("admin@test.com");
        admin.setRole(Role.ADMIN);

        order = new Order();
        order.setUser(user);
        order.setTotalPrice(500.0);
        order.setStatus(OrderStatus.PLACED);
        order.setPaymentMode(PaymentMode.UPI);
    }

    private void mockAuthentication(String email) {

        SecurityContextHolder
                .getContext()
                .setAuthentication(
                        new UsernamePasswordAuthenticationToken(
                                email,
                                null
                        )
                );
    }

    @Test
    void getPaymentHistory_ownerCanAccess() {

        mockAuthentication(user.getEmail());

        PaymentHistory history = new PaymentHistory();
        history.setId(1L);
        history.setOrder(order);
        history.setPaymentStatus(PaymentStatus.SUCCESS);
        history.setPaymentMode(PaymentMode.UPI);
        history.setTransactionId("TXN123");
        history.setAmount(500.0);
        history.setMessage("Success");

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        when(orderRepository.findById(10L))
                .thenReturn(Optional.of(order));

        when(paymentHistoryRepository
                .findByOrderIdOrderByCreatedAtDesc(10L))
                .thenReturn(List.of(history));

        List<PaymentHistoryResponseDTO> result =
                paymentHistoryService.getPaymentHistory(10L);

        assertEquals(1, result.size());

        PaymentHistoryResponseDTO dto = result.get(0);

        assertEquals(1L, dto.getId());
        assertEquals(PaymentStatus.SUCCESS, dto.getPaymentStatus());
        assertEquals(PaymentMode.UPI, dto.getPaymentMode());
        assertEquals("TXN123", dto.getTransactionId());
        assertEquals(500.0, dto.getAmount());
        assertEquals("Success", dto.getMessage());

        verify(paymentHistoryRepository)
                .findByOrderIdOrderByCreatedAtDesc(10L);
    }

    @Test
    void getPaymentHistory_adminCanAccess() {

        mockAuthentication(admin.getEmail());

        when(userRepository.findByEmail(admin.getEmail()))
                .thenReturn(Optional.of(admin));

        when(orderRepository.findById(10L))
                .thenReturn(Optional.of(order));

        when(paymentHistoryRepository
                .findByOrderIdOrderByCreatedAtDesc(10L))
                .thenReturn(List.of());

        List<PaymentHistoryResponseDTO> result =
                paymentHistoryService.getPaymentHistory(10L);

        assertTrue(result.isEmpty());

        verify(paymentHistoryRepository)
                .findByOrderIdOrderByCreatedAtDesc(10L);
    }

    @Test
    void getPaymentHistory_otherUserBlocked() {

        User anotherUser = new User();
        anotherUser.setId(99L);
        anotherUser.setEmail("another@test.com");
        anotherUser.setRole(Role.USER);

        mockAuthentication(anotherUser.getEmail());

        when(userRepository.findByEmail(anotherUser.getEmail()))
                .thenReturn(Optional.of(anotherUser));

        when(orderRepository.findById(10L))
                .thenReturn(Optional.of(order));

        assertThrows(
                UnauthorizedException.class,
                () -> paymentHistoryService.getPaymentHistory(10L)
        );

        verify(paymentHistoryRepository, never())
                .findByOrderIdOrderByCreatedAtDesc(anyLong());
    }
}