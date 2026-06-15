package com.manoj.fastserve.Service;

import com.manoj.fastserve.Entity.MenuItem;
import com.manoj.fastserve.Repository.MenuItemRepository;
import org.junit.jupiter.api.Test;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.data.jpa.domain.Specification;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;


class MenuItemServiceTest {


    private final MenuItemRepository menuItemRepository =
            mock(MenuItemRepository.class);


    private final MenuItemService menuItemService =
            new MenuItemService(menuItemRepository);



    @Test
    void searchMenu_shouldReturnFilteredMenuItems() {


        Pageable pageable =
                PageRequest.of(0,10);


        MenuItem item =
                new MenuItem();

        item.setName("Burger");
        item.setPrice(100.0);



        Page<MenuItem> page =
                new PageImpl<>(
                        List.of(item)
                );



        when(menuItemRepository.findAll(
                any(Specification.class),
                eq(pageable)
        ))
                .thenReturn(page);



        Page<MenuItem> result =
                menuItemService.searchMenu(
                        "burger",
                        50.0,
                        200.0,
                        1L,
                        pageable
                );



        assertEquals(
                1,
                result.getContent().size()
        );


        assertEquals(
                "Burger",
                result.getContent()
                        .get(0)
                        .getName()
        );


        verify(menuItemRepository)
                .findAll(
                        any(Specification.class),
                        eq(pageable)
                );

    }



    @Test
    void searchMenu_whenNoFilters_shouldReturnAll() {


        Pageable pageable =
                PageRequest.of(0,10);


        Page<MenuItem> page =
                new PageImpl<>(
                        List.of()
                );



        when(menuItemRepository.findAll(
                any(Specification.class),
                eq(pageable)
        ))
                .thenReturn(page);



        Page<MenuItem> result =
                menuItemService.searchMenu(
                        null,
                        null,
                        null,
                        null,
                        pageable
                );



        assertNotNull(result);

        assertTrue(
                result.isEmpty()
        );


        verify(menuItemRepository)
                .findAll(
                        any(Specification.class),
                        eq(pageable)
                );

    }

}