package com.manoj.fastserve.Repository;

import com.manoj.fastserve.Entity.Restaurant;
import com.manoj.fastserve.Entity.Review;
import com.manoj.fastserve.Entity.Role;
import com.manoj.fastserve.Entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@MockitoBean(types = CacheManager.class)
class ReviewRepositoryTest {


    @Autowired
    private ReviewRepository reviewRepository;


    @Autowired
    private RestaurantRepository restaurantRepository;


    @Autowired
    private UserRepository userRepository;



    private Restaurant createRestaurant() {

        Restaurant restaurant = new Restaurant();

        restaurant.setName("Tasty Restaurant");
        restaurant.setLocation("Bangalore");

        return restaurantRepository.save(restaurant);
    }



    private User createUser() {

        User user = new User();

        user.setName("Manoj");
        user.setEmail("manoj@test.com");
        user.setPassword("password123");
        user.setAddress("Bangalore");
        user.setRole(Role.USER);

        return userRepository.save(user);
    }



    private Review createReview(
            Restaurant restaurant,
            User user
    ) {

        Review review = new Review();

        review.setRating(5);
        review.setComment("Amazing food");
        review.setRestaurant(restaurant);
        review.setUser(user);

        return reviewRepository.save(review);
    }



    @Test
    void findByRestaurant_shouldReturnReviews() {

        Restaurant restaurant = createRestaurant();
        User user = createUser();


        createReview(
                restaurant,
                user
        );


        List<Review> result =
                reviewRepository.findByRestaurant(
                        restaurant
                );


        assertThat(result).hasSize(1);
        assertThat(result.get(0).getComment())
                .isEqualTo("Amazing food");
    }



    @Test
    void findByRestaurantAndUser_shouldReturnReview() {

        Restaurant restaurant = createRestaurant();
        User user = createUser();


        createReview(
                restaurant,
                user
        );


        var result =
                reviewRepository.findByRestaurantAndUser(
                        restaurant,
                        user
                );


        assertThat(result).isPresent();

        assertThat(result.get().getRating())
                .isEqualTo(5);
    }



    @Test
    void findByRestaurantAndUser_shouldReturnEmpty_whenNoReviewExists() {

        Restaurant restaurant = createRestaurant();
        User user = createUser();


        var result =
                reviewRepository.findByRestaurantAndUser(
                        restaurant,
                        user
                );


        assertThat(result).isEmpty();
    }
}