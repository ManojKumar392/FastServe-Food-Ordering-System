package com.manoj.fastserve.Service;

import com.manoj.fastserve.DTO.CreateOrderRequest;
import com.manoj.fastserve.DTO.OrderItemRequest;
import com.manoj.fastserve.Entity.*;
import com.manoj.fastserve.Exception.BadRequestException;
import com.manoj.fastserve.Exception.ResourceNotFoundException;
import com.manoj.fastserve.Exception.UnauthorizedException;
import com.manoj.fastserve.Repository.MenuItemRepository;
import com.manoj.fastserve.Repository.OrderItemRepository;
import com.manoj.fastserve.Repository.OrderRepository;
import com.manoj.fastserve.Repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import org.springframework.security.core.context.SecurityContextHolder;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final MenuItemRepository menuItemRepository;
    private final UserRepository userRepository;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        MenuItemRepository menuItemRepository,
                        UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.menuItemRepository = menuItemRepository;
        this.userRepository = userRepository;
    }

    // GET all orders
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // GET order by ID
    public Order getOrderById(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        User user = getCurrentUser();
        checkOwnership(order, user);

        return order;
    }

    public Order markAsPaid(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        order.setPaid(true);
        order.setStatus(OrderStatus.PAID);

        return orderRepository.save(order);
    }

    public Order updateStatus(Long id, OrderStatus status) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        order.setStatus(status);

        return orderRepository.save(order);
    }

    public Order createOrderForUser(Long userId, CreateOrderRequest request) {

        User currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.ADMIN &&
                !currentUser.getId().equals(userId)) {
            throw new UnauthorizedException("Access denied");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        Order order = new Order();
        order.setUser(user);
        order.setPaymentMode(request.getPaymentMode());
        order.setPaid(false);
        order.setStatus(OrderStatus.PLACED);

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

        return orderRepository.save(order);
    }

    public List<Order> getOrdersByUserId(Long userId) {

        User currentUser = getCurrentUser();

        if (currentUser.getRole() != Role.ADMIN &&
                !currentUser.getId().equals(userId)) {
            throw new UnauthorizedException("Access denied");
        }

        userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return orderRepository.findByUserId(userId);
    }

    public Order cancelOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order not found"));

        User user = getCurrentUser();
        checkOwnership(order, user);

        if (order.getStatus() == OrderStatus.DELIVERED) {
            throw new BadRequestException("Cannot cancel delivered order");
        }

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new BadRequestException("Order already cancelled");
        }

        order.setStatus(OrderStatus.CANCELLED);

        return orderRepository.save(order);
    }

    private User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private void checkOwnership(Order order, User user) {
        if (user.getRole() == Role.ADMIN) return;

        if (order.getUser() == null ||
                !order.getUser().getId().equals(user.getId())) {
            throw new UnauthorizedException("Access denied: not your order");
        }
    }
}