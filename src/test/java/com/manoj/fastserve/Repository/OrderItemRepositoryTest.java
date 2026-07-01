package com.manoj.fastserve.Repository;

import com.manoj.fastserve.Entity.OrderItem;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.cache.CacheManager;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@MockitoBean(types = CacheManager.class)
class OrderItemRepositoryTest {


    @Autowired
    private OrderItemRepository orderItemRepository;



    @Test
    void save_shouldPersistOrderItem() {

        OrderItem item = new OrderItem();

        item.setMenuItemName("Burger");
        item.setPrice(150.0);
        item.setQuantity(2);


        OrderItem saved =
                orderItemRepository.save(item);


        assertThat(saved.getId())
                .isNotNull();


        assertThat(saved.getMenuItemName())
                .isEqualTo("Burger");


        assertThat(saved.getPrice())
                .isEqualTo(150.0);
    }



    @Test
    void findById_shouldReturnOrderItem() {

        OrderItem item = new OrderItem();

        item.setMenuItemName("Pizza");
        item.setPrice(250.0);
        item.setQuantity(1);


        OrderItem saved =
                orderItemRepository.save(item);


        var result =
                orderItemRepository.findById(saved.getId());


        assertThat(result)
                .isPresent();


        assertThat(result.get().getMenuItemName())
                .isEqualTo("Pizza");
    }
}