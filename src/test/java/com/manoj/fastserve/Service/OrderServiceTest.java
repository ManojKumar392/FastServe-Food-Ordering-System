package com.manoj.fastserve.Service;

import com.manoj.fastserve.DTO.CreateOrderRequest;
import com.manoj.fastserve.DTO.OrderItemRequest;
import com.manoj.fastserve.DTO.OrderResponseDTO;
import com.manoj.fastserve.DTO.PaymentResponse;
import com.manoj.fastserve.Entity.*;
import com.manoj.fastserve.Exception.BadRequestException;
import com.manoj.fastserve.Exception.UnauthorizedException;
import com.manoj.fastserve.Repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


@ExtendWith(MockitoExtension.class)
class OrderServiceTest {


    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderItemRepository orderItemRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PaymentGatewayService paymentGatewayService;


    @InjectMocks
    private OrderService orderService;



    private User user;
    private User admin;

    private MenuItem pizza;


    @BeforeEach
    void setup() {


        user = new User();

        user.setName("Manoj");
        user.setId(1L);
        user.setEmail("manoj@test.com");
        user.setPassword("password");
        user.setAddress("India");
        user.setRole(Role.USER);



        admin = new User();

        admin.setName("Admin");
        admin.setId(2L);
        admin.setEmail("admin@test.com");
        admin.setPassword("password");
        admin.setAddress("India");
        admin.setRole(Role.ADMIN);



        pizza = new MenuItem();

        pizza.setName("Pizza");
        pizza.setPrice(200.0);

    }



    private void mockAuthentication(String email){

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
    void createOrder_success(){


        mockAuthentication(
                user.getEmail()
        );

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        when(menuItemRepository.findById(
                1L
        ))
                .thenReturn(Optional.of(pizza));



        CreateOrderRequest request =
                new CreateOrderRequest();


        request.setPaymentMode(PaymentMode.CASH);

        PaymentResponse paymentResponse = new PaymentResponse(
                PaymentStatus.PENDING,
                null,
                "Cash payment"
        );

        when(paymentGatewayService.processPayment(any()))
                .thenReturn(paymentResponse);

        OrderItemRequest item =
                new OrderItemRequest();

        item.setMenuItemId(1L);
        item.setQuantity(2);



        request.setItems(
                List.of(item)
        );



        when(orderRepository.save(
                any(Order.class)
        ))
                .thenAnswer(
                        invocation -> invocation.getArgument(0)
                );


        OrderResponseDTO result =
                orderService.createOrder(request);

        assertEquals(
                400,
                result.getTotalPrice()
        );


        assertEquals(PaymentStatus.PENDING, result.getPaymentStatus());
        assertFalse(result.getPaid());
        assertEquals(OrderStatus.PLACED, result.getStatus());


        verify(orderRepository,times(2))
                .save(any(Order.class));

    }

    @Test
    void createOrder_onlinePaymentSuccess() {

        mockAuthentication(user.getEmail());

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        when(menuItemRepository.findById(1L))
                .thenReturn(Optional.of(pizza));

        PaymentResponse paymentResponse =
                new PaymentResponse(
                        PaymentStatus.SUCCESS,
                        "TXN123",
                        "Success"
                );

        when(paymentGatewayService.processPayment(any()))
                .thenReturn(paymentResponse);

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(i -> i.getArgument(0));

        CreateOrderRequest request = new CreateOrderRequest();
        request.setPaymentMode(PaymentMode.UPI);

        OrderItemRequest item = new OrderItemRequest();
        item.setMenuItemId(1L);
        item.setQuantity(1);

        request.setItems(List.of(item));

        OrderResponseDTO order = orderService.createOrder(request);

        assertTrue(order.getPaid());
        assertEquals(OrderStatus.PAID, order.getStatus());
        assertEquals(PaymentStatus.SUCCESS, order.getPaymentStatus());
        assertEquals("TXN123", order.getTransactionId());
    }

    @Test
    void createOrder_menuItemNotFound(){


        mockAuthentication(
                user.getEmail()
        );


        when(userRepository.findByEmail(
                user.getEmail()
        ))
                .thenReturn(Optional.of(user));



        when(menuItemRepository.findById(1L))
                .thenReturn(Optional.empty());



        CreateOrderRequest request =
                new CreateOrderRequest();


        request.setPaymentMode(PaymentMode.CASH);


        OrderItemRequest item =
                new OrderItemRequest();


        item.setMenuItemId(1L);
        item.setQuantity(1);


        request.setItems(
                List.of(item)
        );



        assertThrows(
                RuntimeException.class,
                () ->
                        orderService.createOrder(request)
        );

    }





    @Test
    void getOrderById_ownerCanAccess(){


        Order order =
                new Order();


        order.setUser(user);



        mockAuthentication(
                user.getEmail()
        );


        when(orderRepository.findByIdAndUserId(
                1L,
                user.getId()
        ))
                .thenReturn(Optional.of(order));


        when(userRepository.findByEmail(
                user.getEmail()
        ))
                .thenReturn(
                        Optional.of(user)
                );



        OrderResponseDTO result =
                orderService.getOrderById(1L);



        assertEquals(
                user,
                result.getId()
        );

    }


    @Test
    void retryPayment_success() {

        mockAuthentication(user.getEmail());

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PLACED);
        order.setPaymentMode(PaymentMode.UPI);
        order.setPaymentStatus(PaymentStatus.FAILED);
        order.setTotalPrice(200.0);

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        when(orderRepository.findByIdAndUserId(1L, user.getId()))
                .thenReturn(Optional.of(order));

        when(paymentGatewayService.processPayment(any()))
                .thenReturn(new PaymentResponse(
                        PaymentStatus.SUCCESS,
                        "TXN999",
                        "Success"
                ));

        when(orderRepository.save(any(Order.class)))
                .thenAnswer(i -> i.getArgument(0));

        OrderResponseDTO result = orderService.retryPayment(1L);

        assertTrue(result.getPaid());
        assertEquals(OrderStatus.PAID, result.getStatus());
        assertEquals(PaymentStatus.SUCCESS, result.getPaymentStatus());
    }


    @Test
    void retryPayment_cashOrderBlocked() {

        mockAuthentication(user.getEmail());

        Order order = new Order();
        order.setUser(user);
        order.setPaymentMode(PaymentMode.CASH);
        order.setPaymentStatus(PaymentStatus.PENDING);
        order.setStatus(OrderStatus.PLACED);

        when(userRepository.findByEmail(user.getEmail()))
                .thenReturn(Optional.of(user));

        when(orderRepository.findByIdAndUserId(1L, user.getId()))
                .thenReturn(Optional.of(order));

        assertThrows(
                BadRequestException.class,
                () -> orderService.retryPayment(1L)
        );
    }


    @Test
    void getOrderById_otherUserBlocked(){

        User another = new User();

        another.setId(2L);
        another.setRole(Role.USER);


        Order order = new Order();

        order.setUser(another);


        user.setId(1L);
        user.setRole(Role.USER);


        mockAuthentication(
                user.getEmail()
        );


        when(orderRepository.findByIdAndUserId(
                1L,
                user.getId()
        ))
                .thenReturn(Optional.empty());


        when(userRepository.findByEmail(
                user.getEmail()
        ))
                .thenReturn(
                        Optional.of(user)
                );


        assertThrows(
                UnauthorizedException.class,
                () ->
                        orderService.getOrderById(1L)
        );

    }





    @Test
    void cancelOrder_success(){


        Order order =
                new Order();


        order.setUser(user);

        order.setStatus(
                OrderStatus.PLACED
        );


        mockAuthentication(
                user.getEmail()
        );


        when(orderRepository.findByIdAndUserId(
                1L,
                user.getId()
        ))
                .thenReturn(Optional.of(order));


        when(userRepository.findByEmail(
                user.getEmail()
        ))
                .thenReturn(
                        Optional.of(user)
                );


        when(orderRepository.save(order))
                .thenReturn(order);



        OrderResponseDTO result =
                orderService.cancelOrder(1L);



        assertEquals(
                OrderStatus.CANCELLED,
                result.getStatus()
        );

    }





    @Test
    void cancelOrder_deliveredBlocked(){


        Order order =
                new Order();


        order.setUser(user);

        order.setStatus(
                OrderStatus.DELIVERED
        );



        mockAuthentication(
                user.getEmail()
        );



        when(orderRepository.findByIdAndUserId(
                1L,
                user.getId()
        ))
                .thenReturn(Optional.of(order));


        when(userRepository.findByEmail(
                user.getEmail()
        ))
                .thenReturn(
                        Optional.of(user)
                );



        assertThrows(
                BadRequestException.class,
                () ->
                        orderService.cancelOrder(1L)
        );

    }



}