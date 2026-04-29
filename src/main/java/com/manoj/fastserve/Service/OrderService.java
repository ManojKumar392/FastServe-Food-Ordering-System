package com.manoj.fastserve.Service;

import com.manoj.fastserve.DTO.CreateOrderRequest;
import com.manoj.fastserve.DTO.OrderItemRequest;
import com.manoj.fastserve.Entity.*;
import com.manoj.fastserve.Repository.MenuItemRepository;
import com.manoj.fastserve.Repository.OrderItemRepository;
import com.manoj.fastserve.Repository.OrderRepository;
import com.manoj.fastserve.Repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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

    public Order placeOrder(CreateOrderRequest request) {

        Order order = new Order();
        order.setPaymentMode(request.getPaymentMode());
        order.setPaid(false);
        order.setStatus(OrderStatus.PLACED);

        double total = 0;
        List<OrderItem> savedItems = new ArrayList<>();

        order = orderRepository.save(order);

        for (OrderItemRequest itemRequest : request.getItems()) {

            MenuItem menuItem = menuItemRepository.findById(itemRequest.getMenuItemId())
                    .orElseThrow(() -> new RuntimeException("Menu item not found"));

            OrderItem orderItem = new OrderItem();
            orderItem.setOrder(order);
            orderItem.setMenuItemName(menuItem.getName());
            orderItem.setPrice(menuItem.getPrice());
            orderItem.setQuantity(itemRequest.getQuantity());

            total += menuItem.getPrice() * itemRequest.getQuantity();

            savedItems.add(orderItemRepository.save(orderItem));
        }

        order.setTotalPrice(total);
        order.setItems(savedItems);

        return orderRepository.save(order);
    }

    // GET all orders
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // GET order by ID
    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    public Order markAsPaid(Long id) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setPaid(true);
        order.setStatus(OrderStatus.PAID);

        return orderRepository.save(order);
    }

    public Order updateStatus(Long id, OrderStatus status) {

        Order order = orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));

        order.setStatus(status);

        return orderRepository.save(order);
    }

    public Order createOrderForUser(Long userId, CreateOrderRequest request) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

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
                    .orElseThrow(() -> new RuntimeException("Menu item not found"));

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
        userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return orderRepository.findByUserId(userId);
    }
}