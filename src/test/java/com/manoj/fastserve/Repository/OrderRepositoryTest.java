package com.manoj.fastserve.Repository;

import com.manoj.fastserve.Entity.Order;
import com.manoj.fastserve.Entity.OrderStatus;
import com.manoj.fastserve.Entity.Role;
import com.manoj.fastserve.Entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
class OrderRepositoryTest {


    @Autowired
    private OrderRepository orderRepository;


    @Autowired
    private UserRepository userRepository;



    private User createUser() {

        User user = new User();

        user.setName("Manoj");
        user.setEmail("order@test.com");
        user.setPassword("password123");
        user.setAddress("Bangalore");
        user.setRole(Role.USER);

        return userRepository.save(user);
    }



    private Order createOrder(User user) {

        Order order = new Order();

        order.setUser(user);
        order.setTotalPrice(500.0);
        order.setStatus(OrderStatus.PLACED);
        order.setPaymentMode("UPI");
        order.setPaid(false);
        order.setEstimatedDeliveryTime(30);

        return orderRepository.save(order);
    }



    @Test
    void findByUserId_shouldReturnOrdersForUser() {

        User user = createUser();

        createOrder(user);


        var result =
                orderRepository.findByUserId(
                        user.getId(),
                        PageRequest.of(0,10)
                );


        assertThat(result.getContent())
                .hasSize(1);


        assertThat(result.getContent().get(0).getTotalPrice())
                .isEqualTo(500.0);
    }



    @Test
    void findByUserId_shouldReturnEmpty_whenUserHasNoOrders() {

        User user = createUser();


        var result =
                orderRepository.findByUserId(
                        user.getId(),
                        PageRequest.of(0,10)
                );


        assertThat(result.getContent())
                .isEmpty();
    }



    @Test
    void findByUserId_shouldReturnEmpty_whenUserDoesNotExist() {


        var result =
                orderRepository.findByUserId(
                        999L,
                        PageRequest.of(0,10)
                );


        assertThat(result.getContent())
                .isEmpty();
    }
}