package com.manoj.fastserve.Controller;
import org.springframework.web.bind.annotation.*;

import java.awt.*;
import java.util.List;
import com.manoj.fastserve.Entity.MenuItem;
import com.manoj.fastserve.Service.RestaurantService;

@RestController
@RequestMapping("/restaurants")
public class RestaurantController {
    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @GetMapping("/{id}/menu")
    public List<MenuItem> getMenu(@PathVariable Long id) {
        return restaurantService.getMenuByRestaurant(id);
    }

    @PostMapping("/{id}/menu")
    public MenuItem addMenuItem(@PathVariable Long id, @RequestBody MenuItem menuItem){
        return restaurantService.addMenuItem(id, menuItem);
    }
}
