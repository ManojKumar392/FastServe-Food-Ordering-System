package com.manoj.fastserve.Service;

import com.manoj.fastserve.DTO.*;
import com.manoj.fastserve.Entity.*;
import com.manoj.fastserve.Exception.BadRequestException;
import com.manoj.fastserve.Exception.ResourceNotFoundException;
import com.manoj.fastserve.Exception.UnauthorizedException;
import com.manoj.fastserve.Repository.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;
    private final PaymentGatewayService paymentGatewayService;
    private final PaymentHistoryRepository paymentHistoryRepository;
    private final EmailNotificationService emailNotificationService;


    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        MenuItemRepository menuItemRepository,
                        UserRepository userRepository,
                        PaymentGatewayService paymentGatewayService,
                        PaymentHistoryRepository paymentHistoryRepository,
                        EmailNotificationService emailNotificationService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.menuItemRepository = menuItemRepository;
        this.userRepository = userRepository;
        this.paymentGatewayService = paymentGatewayService;
        this.paymentHistoryRepository = paymentHistoryRepository;
        this.emailNotificationService = emailNotificationService;
    }

    // GET all orders
    public Page<OrderResponseDTO> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable)
                .map(this::mapToDTO);
    }

    // GET order by ID
    public OrderResponseDTO getOrderById(Long id) {

        User currentUser = getCurrentUser();

        if (currentUser.getRole() == Role.ADMIN) {
            return mapToDTO(
                    orderRepository.findById(id)
                            .orElseThrow(() ->
                                    new ResourceNotFoundException("Order not found"))
            );
        }

        return mapToDTO(
                orderRepository.findByIdAndUserId(id, currentUser.getId())
                        .orElseThrow(() ->
                                new UnauthorizedException("Access denied: not your order"))
        );
    }

    public OrderResponseDTO markAsPaid(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Cancelled orders cannot be marked as paid");
        }

        if (order.getStatus() == OrderStatus.DELIVERED &&
                order.getPaymentMode() != PaymentMode.CASH) {
            throw new BadRequestException("Delivered online orders cannot be marked as paid");
        }

        if (Boolean.TRUE.equals(order.getPaid())) {
            throw new BadRequestException("Order is already paid");
        }

        order.setPaid(true);
        order.setPaymentTime(LocalDateTime.now());
        order.setStatus(OrderStatus.PAID);
        order.setPaymentStatus(PaymentStatus.SUCCESS);

        PaymentHistory history = new PaymentHistory();
        history.setOrder(order);
        history.setPaymentStatus(PaymentStatus.SUCCESS);
        history.setPaymentMode(order.getPaymentMode());
        history.setAmount(order.getTotalPrice());
        history.setTransactionId(order.getTransactionId());
        history.setMessage("Payment marked as paid by admin");

        paymentHistoryRepository.save(history);

        Order savedOrder = orderRepository.save(order);
        emailNotificationService.sendPaymentResult(savedOrder);
        emailNotificationService.sendOrderStatusChanged(savedOrder);
        return mapToDTO(savedOrder);
    }

    public OrderResponseDTO updateStatus(Long id, OrderStatus status) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        if (order.getStatus() == OrderStatus.DELIVERED &&
                status == OrderStatus.CANCELLED) {

            throw new BadRequestException(
                    "Delivered order cannot be cancelled"
            );
        }

        if (status == OrderStatus.DELIVERED &&
                order.getPaymentMode() != PaymentMode.CASH &&
                !Boolean.TRUE.equals(order.getPaid())) {

            throw new BadRequestException(
                    "Unpaid online orders cannot be delivered"
            );
        }

        OrderStatus previousStatus = order.getStatus();
        order.setStatus(status);

        Order savedOrder = orderRepository.save(order);
        if (previousStatus != status) {
            emailNotificationService.sendOrderStatusChanged(savedOrder);
        }
        return mapToDTO(savedOrder);
    }

    public OrderResponseDTO createOrder(CreateOrderRequest request) {

        User currentUser = getCurrentUser();

        Order order = new Order();
        order.setUser(currentUser);
        order.setPaymentMode(request.getPaymentMode());
        order.setPaid(false);
        order.setStatus(OrderStatus.PLACED);
        order.setPaymentStatus(PaymentStatus.PENDING);

        double total = 0;
        List<OrderItem> savedItems = new ArrayList<>();

        order = orderRepository.save(order);

        for (OrderItemRequest itemRequest : request.getItems()) {

            MenuItem menuItem = menuItemRepository.findById(itemRequest.getMenuItemId())
                    .orElseThrow(() -> new ResourceNotFoundException("Menu item not found"));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setMenuItemName(menuItem.getName());
            orderItem.setPrice(menuItem.getPrice());
            orderItem.setQuantity(itemRequest.getQuantity());

            total += menuItem.getPrice() * itemRequest.getQuantity();

            savedItems.add(orderItemRepository.save(orderItem));
        }

        order.setItems(savedItems);
        order.setTotalPrice(total);

        PaymentResponse paymentResponse =
                processAndRecordPayment(order, total);

        order.setPaymentStatus(paymentResponse.getStatus());
        order.setTransactionId(paymentResponse.getTransactionId());

        switch (paymentResponse.getStatus()) {

            case SUCCESS -> {
                order.setPaid(true);
                order.setStatus(OrderStatus.PAID);
                order.setPaymentTime(LocalDateTime.now());
            }

            case FAILED -> {
                order.setPaid(false);
                order.setStatus(OrderStatus.PLACED);
            }

            case PENDING -> {
                order.setPaid(false);
                order.setStatus(OrderStatus.PLACED);
            }
        }

        Order savedOrder = orderRepository.save(order);
        emailNotificationService.sendOrderPlaced(savedOrder);
        emailNotificationService.sendPaymentResult(savedOrder);
        return mapToDTO(savedOrder);
    }

    public Page<OrderResponseDTO> getMyOrders(Pageable pageable) {
        User currentUser = getCurrentUser();

        return orderRepository.findByUserId(currentUser.getId(), pageable)
                .map(this::mapToDTO);
    }

    public OrderResponseDTO cancelOrder(Long orderId) {

        User currentUser = getCurrentUser();

        Order order;

        if (currentUser.getRole() == Role.ADMIN) {
            order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        } else {
            order = orderRepository.findByIdAndUserId(
                            orderId,
                            currentUser.getId()
                    )
                    .orElseThrow(() ->
                            new UnauthorizedException("Access denied: not your order"));
        }

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new BadRequestException("Cannot cancel delivered order");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Order already cancelled");
        }

        order.setStatus(OrderStatus.CANCELLED);

        Order savedOrder = orderRepository.save(order);
        emailNotificationService.sendOrderStatusChanged(savedOrder);
        return mapToDTO(savedOrder);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private OrderResponseDTO mapToDTO(Order order) {

        OrderResponseDTO dto = new OrderResponseDTO();

        dto.setId(order.getId());
        dto.setTotalPrice(order.getTotalPrice());
        dto.setStatus(order.getStatus());
        dto.setPaymentMode(order.getPaymentMode());
        dto.setPaid(order.getPaid());
        dto.setPaymentStatus(order.getPaymentStatus());
        dto.setTransactionId(order.getTransactionId());
        dto.setEstimatedDeliveryTime(order.getEstimatedDeliveryTime());
        dto.setCreatedAt(order.getCreatedAt());
        dto.setPaymentTime(order.getPaymentTime());

        return dto;
    }

    public OrderResponseDTO retryPayment(Long orderId) {

        User currentUser = getCurrentUser();

        Order order;

        if (currentUser.getRole() == Role.ADMIN) {
            order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new ResourceNotFoundException("Order not found"));
        } else {
            order = orderRepository.findByIdAndUserId(orderId, currentUser.getId())
                    .orElseThrow(() ->
                            new UnauthorizedException("Access denied: not your order"));
        }

        if (order.getPaymentMode() == PaymentMode.CASH) {
            throw new BadRequestException(
                    "Cash on Delivery orders cannot be retried"
            );
        }

        if (order.getPaymentStatus() == PaymentStatus.SUCCESS) {
            throw new BadRequestException(
                    "Payment has already been completed"
            );
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException(
                    "Cannot retry payment for a cancelled order"
            );
        }

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new BadRequestException(
                    "Cannot retry payment for a delivered order"
            );
        }

        if (order.getStatus() != OrderStatus.PLACED) {
            throw new BadRequestException(
                    "Payment can only be retried for placed orders"
            );
        }

        PaymentResponse paymentResponse =
                processAndRecordPayment(order, order.getTotalPrice());

        order.setPaymentStatus(paymentResponse.getStatus());
        if (paymentResponse.getTransactionId() != null) {
            order.setTransactionId(paymentResponse.getTransactionId());
        }

        if (paymentResponse.getStatus() == PaymentStatus.SUCCESS) {
            order.setPaid(true);
            order.setStatus(OrderStatus.PAID);
            order.setPaymentTime(LocalDateTime.now());
        } else {
            order.setPaid(false);
        }

        Order savedOrder = orderRepository.save(order);
        emailNotificationService.sendPaymentResult(savedOrder);
        if (savedOrder.getStatus() == OrderStatus.PAID) {
            emailNotificationService.sendOrderStatusChanged(savedOrder);
        }
        return mapToDTO(savedOrder);
    }

    private PaymentResponse processAndRecordPayment(Order order, Double amount) {

        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setOrderId(order.getId());
        paymentRequest.setAmount(amount);
        paymentRequest.setPaymentMode(order.getPaymentMode());

        PaymentResponse paymentResponse =
                paymentGatewayService.processPayment(paymentRequest);

        PaymentHistory history = new PaymentHistory();
        history.setOrder(order);
        history.setPaymentStatus(paymentResponse.getStatus());
        history.setPaymentMode(order.getPaymentMode());
        history.setTransactionId(paymentResponse.getTransactionId());
        history.setAmount(amount);
        history.setMessage(paymentResponse.getMessage());

        paymentHistoryRepository.save(history);

        return paymentResponse;
    }
}