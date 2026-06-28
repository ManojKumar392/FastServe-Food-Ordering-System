package com.manoj.fastserve.Service;

import com.manoj.fastserve.DTO.CreateRestaurantRequest;
import com.manoj.fastserve.Exception.ResourceNotFoundException;
import com.manoj.fastserve.Repository.spec.RestaurantSpecification;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import java.util.List;
import com.manoj.fastserve.Entity.Restaurant;
import com.manoj.fastserve.Entity.MenuItem;
import com.manoj.fastserve.Repository.RestaurantRepository;
import com.manoj.fastserve.Repository.MenuItemRepository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;

    public RestaurantService(RestaurantRepository restaurantRepository,
                             MenuItemRepository menuItemRepository) {
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
    }

    @Cacheable(
            value = "restaurants",
            key = "#pageable.pageNumber + '-' + #pageable.pageSize"
    )
    public Page<Restaurant> getAllRestaurants(Pageable pageable) {
        return restaurantRepository.findAllByIsDeletedFalse(pageable);
    }

    @Cacheable(value = "restaurantMenu", key = "#restaurantId")
    public List<MenuItem> getMenuByRestaurant(Long restaurantId) {

        Restaurant restaurant = restaurantRepository
                .findByIdAndIsDeletedFalse(restaurantId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Restaurant not found"));

        return menuItemRepository.findByRestaurantAndIsDeletedFalse(restaurant);
    }

    @Caching(evict = {
            @CacheEvict(value = "restaurantMenu", key = "#restaurantId"),
            @CacheEvict(value = "menuSearch", allEntries = true)
    })
    public MenuItem addMenuItem(Long restaurantId, MenuItem menuItem){

        // 1. Get restaurant from DB
        Restaurant restaurant = restaurantRepository
                .findByIdAndIsDeletedFalse(restaurantId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Restaurant not found"));

        // 2. Connect menu item to restaurant
        menuItem.setRestaurant(restaurant);

        // 3. Save menu item
        return menuItemRepository.save(menuItem);
    }

    public List<Restaurant> searchRestaurants(String name, String location) {

        Specification<Restaurant> spec =
                Specification.where(
                                RestaurantSpecification.notDeleted()
                        )
                        .and(RestaurantSpecification.hasName(name))
                        .and(RestaurantSpecification.hasLocation(location));

        return restaurantRepository.findAll(spec);
    }

    @CacheEvict(value = "restaurants", allEntries = true)
    public void softDeleteRestaurant(Long id) {

        Restaurant restaurant = restaurantRepository
                .findByIdAndIsDeletedFalse(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Restaurant not found"));

        restaurant.setIsDeleted(true);

        if (restaurant.getMenuItems() != null) {

            for (MenuItem item : restaurant.getMenuItems()) {
                item.setIsDeleted(true);
                menuItemRepository.save(item);
            }
        }

        restaurantRepository.save(restaurant);
    }

    @CacheEvict(value = "restaurants", allEntries = true)
    public Restaurant restoreRestaurant(Long id) {

        Restaurant restaurant = restaurantRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Restaurant not found"));

        restaurant.setIsDeleted(false);

        if (restaurant.getMenuItems() != null) {

            for (MenuItem item : restaurant.getMenuItems()) {
                item.setIsDeleted(false);
                menuItemRepository.save(item);
            }
        }

        return restaurantRepository.save(restaurant);
    }

    @CacheEvict(value = "restaurants", allEntries = true)
    public Restaurant createRestaurant(CreateRestaurantRequest request) {

        Restaurant restaurant = new Restaurant();

        restaurant.setName(request.getName());
        restaurant.setLocation(request.getLocation());

        return restaurantRepository.save(restaurant);
    }

    @CacheEvict(value = "restaurants", allEntries = true)
    public Restaurant updateRestaurant(
            Long id,
            Restaurant updatedRestaurant
    ) {

        Restaurant restaurant = restaurantRepository
                .findByIdAndIsDeletedFalse(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("Restaurant not found")
                );

        restaurant.setName(updatedRestaurant.getName());
        restaurant.setLocation(updatedRestaurant.getLocation());

        return restaurantRepository.save(restaurant);
    }
}

