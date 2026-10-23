package com.manoj.fastserve.Service;

import com.manoj.fastserve.DTO.BulkMenuImportResult;
import com.manoj.fastserve.Entity.MenuItem;
import com.manoj.fastserve.Entity.Restaurant;
import com.manoj.fastserve.Repository.MenuItemRepository;
import com.manoj.fastserve.Repository.RestaurantRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BulkMenuImportServiceTest {

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private MenuItemRepository menuItemRepository;

    @InjectMocks
    private BulkMenuImportService service;

    @Test
    void importCsv_createsRestaurantAndImportsQuotedItems() {
        when(restaurantRepository
                .findAllByNameIgnoreCaseAndLocationIgnoreCaseAndIsDeletedFalse(
                        "Green Garden", "Central Market"
                ))
                .thenReturn(List.of());
        when(restaurantRepository.save(any(Restaurant.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String csv = "restaurantName,restaurantLocation,itemName,price\r\n"
                + "Green Garden,Central Market,Paneer Bowl,240.00\r\n"
                + "green garden,central market,\"Fresh Lime, Mint\",80.00\r\n";
        BulkMenuImportResult result = service.importCsv(csv.getBytes(StandardCharsets.UTF_8));

        assertTrue(result.successful());
        assertEquals(1, result.restaurantsCreated());
        assertEquals(2, result.menuItemsCreated());

        ArgumentCaptor<Restaurant> restaurant = ArgumentCaptor.forClass(Restaurant.class);
        verify(restaurantRepository).save(restaurant.capture());
        assertEquals("Green Garden", restaurant.getValue().getName());
        assertEquals("Central Market", restaurant.getValue().getLocation());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<MenuItem>> items = ArgumentCaptor.forClass(List.class);
        verify(menuItemRepository).saveAll(items.capture());
        assertEquals(2, items.getValue().size());
        assertEquals("Fresh Lime, Mint", items.getValue().get(1).getName());
        assertEquals(80.0, items.getValue().get(1).getPrice());
        assertSame(restaurant.getValue(), items.getValue().get(0).getRestaurant());
    }

    @Test
    void importCsv_reusesMatchingRestaurant() {
        Restaurant existing = new Restaurant();
        existing.setName("Green Garden");
        existing.setLocation("Central Market");
        when(restaurantRepository
                .findAllByNameIgnoreCaseAndLocationIgnoreCaseAndIsDeletedFalse(
                        "Green Garden", "Central Market"
                ))
                .thenReturn(List.of(existing));

        String csv = "restaurantName,restaurantLocation,itemName,price\n"
                + "Green Garden,Central Market,Paneer Bowl,240\n";
        BulkMenuImportResult result = service.importCsv(csv.getBytes(StandardCharsets.UTF_8));

        assertTrue(result.successful());
        assertEquals(0, result.restaurantsCreated());
        assertEquals(1, result.menuItemsCreated());
        verify(restaurantRepository, never()).save(any(Restaurant.class));
        verify(menuItemRepository).saveAll(argThat(items -> {
            var iterator = items.iterator();
            return iterator.hasNext() && iterator.next().getRestaurant() == existing
                    && !iterator.hasNext();
        }));
    }

    @Test
    void importCsv_rejectsInvalidRowsBeforeWriting() {
        String csv = "restaurantName,restaurantLocation,itemName,price\n"
                + "Green Garden,Central Market,Paneer Bowl,-20\n";

        BulkMenuImportResult result = service.importCsv(csv.getBytes(StandardCharsets.UTF_8));

        assertFalse(result.successful());
        assertTrue(result.errors().get(0).contains("Row 2"));
        verifyNoInteractions(restaurantRepository, menuItemRepository);
    }
}
