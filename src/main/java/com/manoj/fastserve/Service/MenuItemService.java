package com.manoj.fastserve.Service;

import com.manoj.fastserve.Entity.MenuItem;
import com.manoj.fastserve.Repository.MenuItemRepository;
import com.manoj.fastserve.Repository.spec.MenuItemSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MenuItemService {

    private final MenuItemRepository menuItemRepository;

    public MenuItemService(MenuItemRepository menuItemRepository) {
        this.menuItemRepository = menuItemRepository;
    }

    public Page<MenuItem> searchMenu(
            String name,
            Double minPrice,
            Double maxPrice,
            Long restaurantId,
            Pageable pageable
    ) {

        Specification<MenuItem> spec =
                Specification.where(
                                MenuItemSpecification.notDeleted()
                        )
                        .and(MenuItemSpecification.hasName(name))
                        .and(MenuItemSpecification.minPrice(minPrice))
                        .and(MenuItemSpecification.maxPrice(maxPrice))
                        .and(MenuItemSpecification.restaurantId(restaurantId));

        return menuItemRepository.findAll(
                spec,
                pageable
        );
    }
}