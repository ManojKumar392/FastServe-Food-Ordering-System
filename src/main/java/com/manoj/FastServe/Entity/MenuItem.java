package com.manoj.FastServe.Entity;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class MenuItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;
    private Double price;

    @ManyToOne
    @JoinColumn(name = "restaurant_id")
    private Restaurant restaurant;

    @Entity
    @Table(name = "orders") // important (order is reserved keyword in SQL)
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Order {

        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;

        private String status;
        private Double totalPrice;

        @ManyToOne
        @JoinColumn(name = "user_id")
        private User user;

        @ManyToOne
        @JoinColumn(name = "restaurant_id")
        private Restaurant restaurant;

        @OneToMany(mappedBy = "order")
        private List<OrderItem> items;
    }
}