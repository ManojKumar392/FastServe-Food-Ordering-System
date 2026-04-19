package com.manoj.fastserve.Service;

import org.springframework.stereotype.Service;
import java.util.List;
import com.manoj.fastserve.Entity.Restaurant;
import com.manoj.fastserve.Entity.MenuItem;
import com.manoj.fastserve.Repository.RestaurantRepository;
import com.manoj.fastserve.Repository.MenuItemRepository;

@Service
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;

    public RestaurantService(RestaurantRepository restaurantRepository,
                             MenuItemRepository menuItemRepository) {
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
    }

    public List<Restaurant> getAllRestaurants() {
        return restaurantRepository.findAll();
    }

    public List<MenuItem> getMenuByRestaurant(Long restaurantId) {
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        return menuItemRepository.findByRestaurant(restaurant);
    }

    public MenuItem addMenuItem(Long restaurantId, MenuItem menuItem){

        // 1. Get restaurant from DB
        Restaurant restaurant = restaurantRepository.findById(restaurantId)
                .orElseThrow(() -> new RuntimeException("Restaurant not found"));

        // 2. Connect menu item to restaurant
        menuItem.setRestaurant(restaurant);

        // 3. Save menu item
        return menuItemRepository.save(menuItem);
    }
}