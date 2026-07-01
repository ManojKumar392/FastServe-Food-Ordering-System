package com.manoj.fastserve.Repository;

import com.manoj.fastserve.Entity.Restaurant;
import com.manoj.fastserve.Repository.spec.RestaurantSpecification;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@MockitoBean(types = CacheManager.class)
class RestaurantRepositoryTest {


    @Autowired
    private RestaurantRepository restaurantRepository;


    private Restaurant createRestaurant(String name, String location) {

        Restaurant restaurant = new Restaurant();
        restaurant.setName(name);
        restaurant.setLocation(location);

        return restaurantRepository.save(restaurant);
    }


    @Test
    void findByNameContainingIgnoreCase_shouldReturnRestaurant() {

        createRestaurant("Pizza Palace", "Bangalore");


        Page<Restaurant> result =
                restaurantRepository.findByNameContainingIgnoreCase(
                        "pizza",
                        PageRequest.of(0,10)
                );


        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getName())
                .isEqualTo("Pizza Palace");
    }


    @Test
    void findByLocationContainingIgnoreCase_shouldReturnRestaurant() {

        createRestaurant("Burger House", "Chennai");


        Page<Restaurant> result =
                restaurantRepository.findByLocationContainingIgnoreCase(
                        "chennai",
                        PageRequest.of(0,10)
                );


        assertThat(result.getContent()).hasSize(1);
    }


    @Test
    void findByNameAndLocation_shouldReturnRestaurant() {

        createRestaurant("Food Corner", "Mysore");


        Page<Restaurant> result =
                restaurantRepository.findByNameContainingIgnoreCaseAndLocationContainingIgnoreCase(
                        "food",
                        "mysore",
                        PageRequest.of(0,10)
                );


        assertThat(result.getContent()).hasSize(1);
    }


    @Test
    void findByIdAndIsDeletedFalse_shouldReturnRestaurant() {

        Restaurant saved = createRestaurant(
                "Healthy Kitchen",
                "Bangalore"
        );


        var result =
                restaurantRepository.findByIdAndIsDeletedFalse(saved.getId());


        assertThat(result).isPresent();
    }


    @Test
    void findAllByIsDeletedFalse_shouldIgnoreDeletedRestaurants() {

        Restaurant active = createRestaurant(
                "Active Restaurant",
                "Delhi"
        );


        Restaurant deleted = createRestaurant(
                "Deleted Restaurant",
                "Delhi"
        );

        deleted.setIsDeleted(true);

        restaurantRepository.save(deleted);


        Page<Restaurant> result =
                restaurantRepository.findAllByIsDeletedFalse(
                        PageRequest.of(0,10)
                );


        assertThat(result.getContent())
                .extracting(Restaurant::getName)
                .contains("Active Restaurant")
                .doesNotContain("Deleted Restaurant");
    }


    @Test
    void specification_shouldSearchRestaurantByName() {

        createRestaurant(
                "Dominos",
                "Bangalore"
        );


        Page<Restaurant> result =
                restaurantRepository.findAll(
                        RestaurantSpecification.hasName("dom"),
                        PageRequest.of(0,10)
                );


        assertThat(result.getContent()).hasSize(1);
    }
}