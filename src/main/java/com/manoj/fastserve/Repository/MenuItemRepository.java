package com.manoj.fastserve.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.manoj.fastserve.Entity.MenuItem;
import com.manoj.fastserve.Entity.Restaurant;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MenuItemRepository
        extends JpaRepository<MenuItem, Long>,
        JpaSpecificationExecutor<MenuItem> {

    List<MenuItem> findByRestaurant(Restaurant restaurant);

    List<MenuItem> findByNameContainingIgnoreCase(String name);

    List<MenuItem> findByRestaurantAndIsDeletedFalse(
            Restaurant restaurant
    );
}
