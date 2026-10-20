package com.manoj.fastserve.Service;

import com.manoj.fastserve.DTO.PaymentHistoryResponseDTO;
import com.manoj.fastserve.Entity.Order;
import com.manoj.fastserve.Entity.Role;
import com.manoj.fastserve.Entity.User;
import com.manoj.fastserve.Exception.ResourceNotFoundException;
import com.manoj.fastserve.Exception.UnauthorizedException;
import com.manoj.fastserve.Repository.OrderRepository;
import com.manoj.fastserve.Repository.PaymentHistoryRepository;
import com.manoj.fastserve.Repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PaymentHistoryService {

    private final PaymentHistoryRepository paymentHistoryRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    public PaymentHistoryService(
            PaymentHistoryRepository paymentHistoryRepository,
            OrderRepository orderRepository,
            UserRepository userRepository) {

        this.paymentHistoryRepository = paymentHistoryRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    public List<PaymentHistoryResponseDTO> getPaymentHistory(Long orderId) {

        User currentUser = getCurrentUser();

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Order not found"));

        if (currentUser.getRole() != Role.ADMIN &&
                !order.getUser().getId().equals(currentUser.getId())) {

            throw new UnauthorizedException(
                    "Access denied: not your order"
            );
        }

        return paymentHistoryRepository
                .findByOrderIdOrderByCreatedAtDesc(orderId)
                .stream()
                .map(this::mapToDTO)
                .toList();
    }

    private PaymentHistoryResponseDTO mapToDTO(
            com.manoj.fastserve.Entity.PaymentHistory history) {

        PaymentHistoryResponseDTO dto =
                new PaymentHistoryResponseDTO();

        dto.setId(history.getId());
        dto.setPaymentStatus(history.getPaymentStatus());
        dto.setPaymentMode(history.getPaymentMode());
        dto.setTransactionId(history.getTransactionId());
        dto.setAmount(history.getAmount());
        dto.setMessage(history.getMessage());
        dto.setCreatedAt(history.getCreatedAt());

        return dto;
    }

    private User getCurrentUser() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));
    }
}
