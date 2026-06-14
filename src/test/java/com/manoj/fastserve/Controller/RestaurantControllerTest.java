package com.manoj.fastserve.Controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.manoj.fastserve.Entity.MenuItem;
import com.manoj.fastserve.Entity.Restaurant;
import com.manoj.fastserve.Service.RestaurantService;

import com.manoj.fastserve.Util.JwtUtil;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.context.bean.override.mockito.MockitoBean;


import java.util.List;


import static org.mockito.Mockito.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;



@WebMvcTest(RestaurantController.class)
@AutoConfigureMockMvc(addFilters = false)
class RestaurantControllerTest {


    @Autowired
    private MockMvc mockMvc;


    @MockitoBean
    private RestaurantService restaurantService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @Test
    void getAllRestaurants_shouldReturnPage() throws Exception {


        Restaurant restaurant = new Restaurant();

        restaurant.setName("Pizza House");
        restaurant.setLocation("Bangalore");


        Page<Restaurant> page =
                new PageImpl<>(
                        List.of(restaurant),
                        PageRequest.of(0,10),
                        1
                );


        when(restaurantService.getAllRestaurants(any()))
                .thenReturn(page);



        mockMvc.perform(
                        get("/restaurants")
                                .param("page","0")
                                .param("size","10")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].name")
                        .value("Pizza House"));

    }




    @Test
    void getMenu_shouldReturnMenuItems() throws Exception {


        MenuItem item = new MenuItem();

        item.setName("Burger");
        item.setPrice(100.0);



        when(restaurantService.getMenuByRestaurant(1L))
                .thenReturn(
                        List.of(item)
                );



        mockMvc.perform(
                        get("/restaurants/1/menu")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name")
                        .value("Burger"));

    }





    @Test
    void addMenuItem_shouldCreateItem() throws Exception {


        MenuItem item = new MenuItem();

        item.setName("Pizza");
        item.setPrice(250.0);



        when(restaurantService.addMenuItem(
                eq(1L),
                any(MenuItem.class)
        ))
                .thenReturn(item);



        mockMvc.perform(
                        post("/restaurants/1/menu")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("""
                {
                  "name":"Pizza",
                  "price":250
                }
                """)
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name")
                        .value("Pizza"));

    }





    @Test
    void searchRestaurants_shouldReturnResults() throws Exception {


        Restaurant restaurant = new Restaurant();

        restaurant.setName("Dominos");


        when(restaurantService.searchRestaurants(
                "Domi",
                null
        ))
                .thenReturn(
                        List.of(restaurant)
                );



        mockMvc.perform(
                        get("/restaurants/search")
                                .param("name","Domi")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name")
                        .value("Dominos"));

    }





    @Test
    void deleteRestaurant_shouldReturnSuccess() throws Exception {


        doNothing()
                .when(restaurantService)
                .softDeleteRestaurant(1L);



        mockMvc.perform(
                        delete("/restaurants/1")
                )
                .andExpect(status().isOk())
                .andExpect(content()
                        .string("Restaurant deleted"));

    }





    @Test
    void restoreRestaurant_shouldReturnRestaurant() throws Exception {


        Restaurant restaurant = new Restaurant();

        restaurant.setName("Restored");


        when(restaurantService.restoreRestaurant(1L))
                .thenReturn(restaurant);



        mockMvc.perform(
                        put("/restaurants/1/restore")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name")
                        .value("Restored"));

    }


}