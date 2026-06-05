package com.manoj.fastserve.Controller;

import com.manoj.fastserve.Entity.MenuItem;
import com.manoj.fastserve.Service.MenuItemService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/menu")
public class MenuItemController {

    private final MenuItemService menuItemService;

    public MenuItemController(MenuItemService menuItemService) {
        this.menuItemService = menuItemService;
    }

    @GetMapping("/search")
    public Page<MenuItem> searchMenu(

            @RequestParam(required = false)
            String name,

            @RequestParam(required = false)
            Double minPrice,

            @RequestParam(required = false)
            Double maxPrice,

            @RequestParam(required = false)
            Long restaurantId,

            Pageable pageable
    ) {

        return menuItemService.searchMenu(
                name,
                minPrice,
                maxPrice,
                restaurantId,
                pageable
        );
    }
}