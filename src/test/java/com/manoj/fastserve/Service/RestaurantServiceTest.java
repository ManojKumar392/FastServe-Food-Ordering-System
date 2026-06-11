package com.manoj.fastserve.Service;


import com.manoj.fastserve.Entity.MenuItem;
import com.manoj.fastserve.Entity.Restaurant;
import com.manoj.fastserve.Exception.ResourceNotFoundException;
import com.manoj.fastserve.Repository.MenuItemRepository;
import com.manoj.fastserve.Repository.RestaurantRepository;


import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;


import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;


import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;


import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;



@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {


    @Mock
    private RestaurantRepository restaurantRepository;


    @Mock
    private MenuItemRepository menuItemRepository;



    @InjectMocks
    private RestaurantService restaurantService;




    @Test
    void getAllRestaurants_shouldReturnActiveRestaurants(){


        Page<Restaurant> page =
                new PageImpl<>(
                        List.of(new Restaurant())
                );


        when(
                restaurantRepository.findAllByIsDeletedFalse(
                        PageRequest.of(0,10)
                )
        )
                .thenReturn(page);



        Page<Restaurant> result =
                restaurantService.getAllRestaurants(
                        PageRequest.of(0,10)
                );



        assertEquals(
                1,
                result.getTotalElements()
        );


        verify(restaurantRepository)
                .findAllByIsDeletedFalse(
                        PageRequest.of(0,10)
                );

    }





    @Test
    void getMenuByRestaurant_shouldReturnMenu(){


        Restaurant restaurant =
                new Restaurant();



        when(
                restaurantRepository.findByIdAndIsDeletedFalse(1L)
        )
                .thenReturn(Optional.of(restaurant));



        List<MenuItem> items =
                List.of(new MenuItem());



        when(
                menuItemRepository.findByRestaurantAndIsDeletedFalse(
                        restaurant
                )
        )
                .thenReturn(items);



        List<MenuItem> result =
                restaurantService.getMenuByRestaurant(1L);



        assertEquals(
                1,
                result.size()
        );


    }





    @Test
    void getMenuByRestaurant_shouldThrowIfDeleted(){


        when(
                restaurantRepository.findByIdAndIsDeletedFalse(1L)
        )
                .thenReturn(Optional.empty());



        assertThrows(
                ResourceNotFoundException.class,
                () ->
                        restaurantService.getMenuByRestaurant(1L)
        );

    }







    @Test
    void softDeleteRestaurant_shouldDeleteRestaurantAndMenu(){



        Restaurant restaurant =
                new Restaurant();



        MenuItem item =
                new MenuItem();



        List<MenuItem> items =
                new ArrayList<>();

        items.add(item);


        restaurant.setMenuItems(items);



        when(
                restaurantRepository.findByIdAndIsDeletedFalse(1L)
        )
                .thenReturn(Optional.of(restaurant));



        restaurantService.softDeleteRestaurant(1L);



        assertTrue(
                restaurant.getIsDeleted()
        );


        assertTrue(
                item.getIsDeleted()
        );



        verify(menuItemRepository)
                .save(item);



        verify(restaurantRepository)
                .save(restaurant);


    }







    @Test
    void restoreRestaurant_shouldRestoreRestaurantAndMenu(){



        Restaurant restaurant =
                new Restaurant();



        MenuItem item =
                new MenuItem();



        restaurant.setMenuItems(
                List.of(item)
        );



        when(
                restaurantRepository.findById(1L)
        )
                .thenReturn(Optional.of(restaurant));



        restaurantService.restoreRestaurant(1L);



        assertFalse(
                restaurant.getIsDeleted()
        );


        assertFalse(
                item.getIsDeleted()
        );



        verify(menuItemRepository)
                .save(item);



        verify(restaurantRepository)
                .save(restaurant);

    }

}