package com.manoj.fastserve.Entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.List;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(
        name = "orders",
        indexes = {
                @Index(
                        name = "idx_order_user",
                        columnList = "user_id"
                ),
                @Index(
                        name = "idx_order_status",
                        columnList = "status"
                )
        }
)
public class Order extends BaseEntity{

    @Getter
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Setter
    @Getter
    private Double totalPrice;

    @Setter
    @Getter
    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Setter
    @Getter
    @Enumerated(EnumType.STRING)
    private PaymentMode paymentMode;

    @Setter
    @Getter
    private Boolean paid;

    @Setter
    @Getter
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    @Setter
    @Getter
    private String transactionId;

    @Setter
    @Getter
    private Integer estimatedDeliveryTime;

    @Setter
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items;

    @Setter
    @Getter
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    @JsonManagedReference
    public List<OrderItem> getItems() {
        return items;
    }

}