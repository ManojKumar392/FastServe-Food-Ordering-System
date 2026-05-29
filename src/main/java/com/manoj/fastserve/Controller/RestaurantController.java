package com.manoj.fastserve.Controller;

import com.manoj.fastserve.Entity.MenuItem;
import com.manoj.fastserve.Service.RestaurantService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/restaurants")
public class RestaurantController {
    private final RestaurantService restaurantService;

    public RestaurantController(RestaurantService restaurantService) {
        this.restaurantService = restaurantService;
    }

    @GetMapping("/{id}/menu")
    public ResponseEntity<List<MenuItem>> getMenu(@PathVariable Long id) {
        return ResponseEntity.ok(
                restaurantService.getMenuByRestaurant(id)
        );
    }

    @PostMapping("/{id}/menu")
    public ResponseEntity<MenuItem> addMenuItem(
            @PathVariable Long id,
            @Valid @RequestBody MenuItem menuItem){

        return new ResponseEntity<>(
                restaurantService.addMenuItem(id, menuItem),
                HttpStatus.CREATED
        );
    }
}
