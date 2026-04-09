package com.manoj.fastserve.Repository;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import com.manoj.fastserve.Entity.MenuItem;
import com.manoj.fastserve.Entity.Restaurant;

public interface MenuItemRepository extends JpaRepository<MenuItem, Long> {

    List<MenuItem> findByRestaurant(Restaurant restaurant);
}
