package com.manoj.fastserve.Repository;

import com.manoj.fastserve.Entity.Review;
import com.manoj.fastserve.Entity.Restaurant;
import com.manoj.fastserve.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Long> {

    List<Review> findByRestaurant(Restaurant restaurant);

    Optional<Review> findByRestaurantAndUser(
            Restaurant restaurant,
            User user
    );
}