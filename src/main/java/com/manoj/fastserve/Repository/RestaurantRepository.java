package com.manoj.fastserve.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import com.manoj.fastserve.Entity.Restaurant;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long> {
}