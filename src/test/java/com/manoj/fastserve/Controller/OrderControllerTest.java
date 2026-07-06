package com.manoj.fastserve.Controller;

import com.manoj.fastserve.Config.TestCacheConfig;
import com.manoj.fastserve.DTO.CreateOrderRequest;
import com.manoj.fastserve.DTO.OrderResponseDTO;
import com.manoj.fastserve.Entity.Order;
import com.manoj.fastserve.Entity.OrderStatus;
import com.manoj.fastserve.Service.OrderService;

import com.manoj.fastserve.Util.JwtUtil;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestCacheConfig.class)
@WebMvcTest(OrderController.class)
@AutoConfigureMockMvc(addFilters = false)
class OrderControllerTest {


    @Autowired
    private MockMvc mockMvc;


    @MockitoBean
    private OrderService orderService;

    @MockitoBean
    private JwtUtil jwtUtil;



    @Test
    void getAllOrders_shouldReturnOrders() throws Exception {


        Page<Order> page =
                new PageImpl<>(
                        java.util.List.of(new Order())
                );


        when(orderService.getAllOrders(any(Pageable.class)))
                .thenReturn(page);



        mockMvc.perform(
                        get("/orders")
                )
                .andExpect(status().isOk());

    }





    @Test
    void getOrderById_shouldReturnOrder() throws Exception {


        OrderResponseDTO response = new OrderResponseDTO();


        when(orderService.getOrderById(1L))
                .thenReturn(response);



        mockMvc.perform(
                        get("/orders/1")
                )
                .andExpect(status().isOk());

    }





    @Test
    void markAsPaid_shouldReturnUpdatedOrder() throws Exception {


        OrderResponseDTO response = new OrderResponseDTO();


        when(orderService.markAsPaid(1L))
                .thenReturn(response);



        mockMvc.perform(
                        patch("/orders/1/pay")
                )
                .andExpect(status().isOk());

    }





    @Test
    void updateStatus_shouldReturnUpdatedOrder() throws Exception {


        OrderResponseDTO response = new OrderResponseDTO();


        when(orderService.updateStatus(
                eq(1L),
                eq(OrderStatus.PAID)
        ))
                .thenReturn(response);



        mockMvc.perform(
                        patch("/orders/1/status")
                                .param(
                                        "status",
                                        "PAID"
                                )
                )
                .andExpect(status().isOk());

    }





    @Test
    void createOrder_shouldCreateOrder() throws Exception {

        OrderResponseDTO response = new OrderResponseDTO();

        when(orderService.createOrder(
                any(CreateOrderRequest.class)
        ))
                .thenReturn(response);

        mockMvc.perform(
                        post("/orders")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                            {
                              "paymentMode":"UPI",
                              "items":[
                                {
                                  "menuItemId":1,
                                  "quantity":2
                                }
                              ]
                            }
                            """)
                )
                .andExpect(status().isCreated());

    }





    @Test
    void getMyOrders_shouldReturnOrders() throws Exception {

        Page<Order> page =
                new PageImpl<>(
                        java.util.List.of(new Order())
                );

        when(orderService.getMyOrders(
                any(Pageable.class)
        ))
                .thenReturn(page);

        mockMvc.perform(
                        get("/orders/my")
                )
                .andExpect(status().isOk());

    }





    @Test
    void cancelOrder_shouldCancelOrder() throws Exception {


        OrderResponseDTO response = new OrderResponseDTO();


        when(orderService.cancelOrder(1L))
                .thenReturn(response);



        mockMvc.perform(
                        patch("/orders/1/cancel")
                )
                .andExpect(status().isOk());

    }

    @Test
    void retryPayment_shouldReturnUpdatedOrder() throws Exception {

        OrderResponseDTO response = new OrderResponseDTO();

        when(orderService.retryPayment(1L))
                .thenReturn(response);

        mockMvc.perform(
                        post("/orders/1/retry-payment")
                )
                .andExpect(status().isOk());
    }

}