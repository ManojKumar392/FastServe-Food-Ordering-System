package com.manoj.fastserve.Repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.manoj.fastserve.Entity.Restaurant;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface RestaurantRepository extends JpaRepository<Restaurant, Long>, JpaSpecificationExecutor<Restaurant> {

    Page<Restaurant> findByNameContainingIgnoreCase(String name, Pageable pageable);

    Page<Restaurant> findByLocationContainingIgnoreCase(String location, Pageable pageable);

    Page<Restaurant> findByNameContainingIgnoreCaseAndLocationContainingIgnoreCase(
            String name,
            String location,
            Pageable pageable
    );

    Optional<Restaurant> findByIdAndIsDeletedFalse(Long id);

    Page<Restaurant> findAllByIsDeletedFalse(Pageable pageable);
}