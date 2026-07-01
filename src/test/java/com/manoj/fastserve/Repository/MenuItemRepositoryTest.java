package com.manoj.fastserve.Repository;

import com.manoj.fastserve.Entity.MenuItem;
import com.manoj.fastserve.Entity.Restaurant;
import com.manoj.fastserve.Repository.spec.MenuItemSpecification;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@MockitoBean(types = CacheManager.class)
class MenuItemRepositoryTest {


    @Autowired
    private MenuItemRepository menuItemRepository;


    @Autowired
    private RestaurantRepository restaurantRepository;


    private Restaurant createRestaurant() {

        Restaurant restaurant = new Restaurant();
        restaurant.setName("Food Hub");
        restaurant.setLocation("Bangalore");

        return restaurantRepository.save(restaurant);
    }


    private MenuItem createMenuItem(
            String name,
            Double price,
            Restaurant restaurant
    ) {

        MenuItem item = new MenuItem();

        item.setName(name);
        item.setPrice(price);
        item.setRestaurant(restaurant);

        return menuItemRepository.save(item);
    }


    @Test
    void findByRestaurant_shouldReturnItems() {

        Restaurant restaurant = createRestaurant();

        createMenuItem(
                "Burger",
                120.0,
                restaurant
        );


        List<MenuItem> result =
                menuItemRepository.findByRestaurant(restaurant);


        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName())
                .isEqualTo("Burger");
    }


    @Test
    void findByNameContainingIgnoreCase_shouldReturnMatchingItems() {

        Restaurant restaurant = createRestaurant();

        createMenuItem(
                "Chicken Pizza",
                250.0,
                restaurant
        );


        List<MenuItem> result =
                menuItemRepository.findByNameContainingIgnoreCase(
                        "pizza"
                );


        assertThat(result).hasSize(1);
    }


    @Test
    void findByRestaurantAndIsDeletedFalse_shouldIgnoreDeletedItems() {

        Restaurant restaurant = createRestaurant();


        createMenuItem(
                "Active Item",
                100.0,
                restaurant
        );


        MenuItem deleted =
                createMenuItem(
                        "Deleted Item",
                        200.0,
                        restaurant
                );


        deleted.setIsDeleted(true);

        menuItemRepository.save(deleted);


        List<MenuItem> result =
                menuItemRepository.findByRestaurantAndIsDeletedFalse(
                        restaurant
                );


        assertThat(result)
                .extracting(MenuItem::getName)
                .contains("Active Item")
                .doesNotContain("Deleted Item");
    }


    @Test
    void specification_shouldFindByName() {

        Restaurant restaurant = createRestaurant();

        createMenuItem(
                "Paneer Roll",
                150.0,
                restaurant
        );


        var result =
                menuItemRepository.findAll(
                        MenuItemSpecification.hasName("paneer")
                );


        assertThat(result).hasSize(1);
    }


    @Test
    void specification_shouldFilterByPriceRange() {

        Restaurant restaurant = createRestaurant();

        createMenuItem(
                "Cheap Item",
                50.0,
                restaurant
        );

        createMenuItem(
                "Medium Item",
                200.0,
                restaurant
        );


        var result =
                menuItemRepository.findAll(
                        MenuItemSpecification.minPrice(100.0)
                                .and(MenuItemSpecification.maxPrice(300.0))
                );


        assertThat(result)
                .extracting(MenuItem::getName)
                .containsExactly("Medium Item")
                .doesNotContain("Cheap Item");
    }


    @Test
    void specification_shouldFindByRestaurantId() {

        Restaurant restaurant = createRestaurant();

        createMenuItem(
                "Dosa",
                80.0,
                restaurant
        );


        var result =
                menuItemRepository.findAll(
                        MenuItemSpecification.restaurantId(
                                restaurant.getId()
                        )
                );


        assertThat(result).hasSize(1);
    }


    @Test
    void specification_notDeleted_shouldExcludeDeletedItems() {

        Restaurant restaurant = createRestaurant();


        createMenuItem(
                "Available",
                100.0,
                restaurant
        );


        MenuItem deleted =
                createMenuItem(
                        "Removed",
                        100.0,
                        restaurant
                );

        deleted.setIsDeleted(true);

        menuItemRepository.save(deleted);


        var result =
                menuItemRepository.findAll(
                        MenuItemSpecification.notDeleted()
                );


        assertThat(result)
                .extracting(MenuItem::getName)
                .contains("Available")
                .doesNotContain("Removed");
    }
}