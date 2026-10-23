package com.manoj.fastserve.Service;

import com.manoj.fastserve.DTO.BulkMenuImportResult;
import com.manoj.fastserve.Entity.MenuItem;
import com.manoj.fastserve.Entity.Restaurant;
import com.manoj.fastserve.Repository.MenuItemRepository;
import com.manoj.fastserve.Repository.RestaurantRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class BulkMenuImportService {

    private static final int MAX_ROWS = 10_000;
    private static final int MAX_FILE_SIZE = 2 * 1024 * 1024;
    private static final List<String> REQUIRED_HEADERS =
            List.of("restaurantname", "restaurantlocation", "itemname", "price");

    private final RestaurantRepository restaurantRepository;
    private final MenuItemRepository menuItemRepository;

    public BulkMenuImportService(
            RestaurantRepository restaurantRepository,
            MenuItemRepository menuItemRepository) {
        this.restaurantRepository = restaurantRepository;
        this.menuItemRepository = menuItemRepository;
    }

    @Transactional
    @Caching(evict = {
            @CacheEvict(value = "restaurants", allEntries = true),
            @CacheEvict(value = "restaurantMenu", allEntries = true),
            @CacheEvict(value = "menuSearch", allEntries = true)
    })
    public BulkMenuImportResult importCsv(byte[] content) {
        if (content == null || content.length == 0) {
            return BulkMenuImportResult.failure(List.of("The CSV file is empty."));
        }
        if (content.length > MAX_FILE_SIZE) {
            return BulkMenuImportResult.failure(List.of("The CSV file exceeds the 2 MB limit."));
        }

        String csv = new String(content, java.nio.charset.StandardCharsets.UTF_8);
        if (csv.startsWith("\uFEFF")) {
            csv = csv.substring(1);
        }
        List<List<String>> rows;
        try {
            rows = parseCsv(csv);
        } catch (CsvFormatException exception) {
            return BulkMenuImportResult.failure(List.of(exception.getMessage()));
        }
        if (rows.isEmpty()) {
            return BulkMenuImportResult.failure(List.of("The CSV file has no header row."));
        }

        Map<String, Integer> columns = headerColumns(rows.get(0));
        if (columns == null) {
            return BulkMenuImportResult.failure(List.of(
                    "The header must contain restaurantName, restaurantLocation, itemName, and price."
            ));
        }

        List<String> errors = new ArrayList<>();
        Map<RestaurantKey, List<ImportRow>> groupedRows = new LinkedHashMap<>();
        int dataRows = 0;
        for (int index = 1; index < rows.size(); index++) {
            List<String> row = rows.get(index);
            int rowNumber = index + 1;
            if (row.stream().allMatch(String::isBlank)) {
                continue;
            }
            dataRows++;
            if (dataRows > MAX_ROWS) {
                errors.add("The CSV file exceeds the " + MAX_ROWS + " data row limit.");
                break;
            }
            if (row.size() != columns.size()) {
                errors.add("Row " + rowNumber + ": expected 4 columns but found " + row.size() + ".");
                continue;
            }

            String restaurantName = value(row, columns, "restaurantname");
            String location = value(row, columns, "restaurantlocation");
            String itemName = value(row, columns, "itemname");
            String priceValue = value(row, columns, "price");
            if (restaurantName.isBlank() || location.isBlank() || itemName.isBlank()) {
                errors.add("Row " + rowNumber + ": restaurant name, location, and item name are required.");
                continue;
            }

            BigDecimal price;
            double parsedPrice;
            try {
                price = new BigDecimal(priceValue);
                parsedPrice = price.doubleValue();
                if (price.signum() <= 0 || parsedPrice <= 0 || !Double.isFinite(parsedPrice)) {
                    throw new NumberFormatException();
                }
            } catch (NumberFormatException exception) {
                errors.add("Row " + rowNumber + ": price must be a positive number.");
                continue;
            }

            RestaurantKey key = new RestaurantKey(
                    restaurantName.toLowerCase(Locale.ROOT),
                    location.toLowerCase(Locale.ROOT)
            );
            groupedRows.computeIfAbsent(key, ignored -> new ArrayList<>())
                    .add(new ImportRow(restaurantName, location, itemName, parsedPrice));
        }
        if (!errors.isEmpty()) {
            return BulkMenuImportResult.failure(errors);
        }
        if (groupedRows.isEmpty()) {
            return BulkMenuImportResult.failure(List.of("The CSV file contains no menu item rows."));
        }

        Map<RestaurantKey, Restaurant> restaurants = new LinkedHashMap<>();
        for (RestaurantKey key : groupedRows.keySet()) {
            List<Restaurant> matches = restaurantRepository
                    .findAllByNameIgnoreCaseAndLocationIgnoreCaseAndIsDeletedFalse(
                            groupedRows.get(key).get(0).restaurantName(),
                            groupedRows.get(key).get(0).location()
                    );
            if (matches.size() > 1) {
                errors.add("Multiple active restaurants match \""
                        + groupedRows.get(key).get(0).restaurantName()
                        + "\" in \"" + groupedRows.get(key).get(0).location() + "\".");
            } else {
                restaurants.put(key, matches.isEmpty() ? null : matches.get(0));
            }
        }
        if (!errors.isEmpty()) {
            return BulkMenuImportResult.failure(errors);
        }

        int restaurantsCreated = 0;
        List<MenuItem> items = new ArrayList<>();
        for (Map.Entry<RestaurantKey, List<ImportRow>> group : groupedRows.entrySet()) {
            Restaurant restaurant = restaurants.get(group.getKey());
            if (restaurant == null) {
                restaurant = new Restaurant();
                restaurant.setName(group.getValue().get(0).restaurantName());
                restaurant.setLocation(group.getValue().get(0).location());
                restaurant = restaurantRepository.save(restaurant);
                restaurantsCreated++;
            }

            for (ImportRow row : group.getValue()) {
                MenuItem menuItem = new MenuItem();
                menuItem.setName(row.itemName());
                menuItem.setPrice(row.price());
                menuItem.setRestaurant(restaurant);
                items.add(menuItem);
            }
        }

        menuItemRepository.saveAll(items);
        return BulkMenuImportResult.success(restaurantsCreated, items.size());
    }

    private Map<String, Integer> headerColumns(List<String> header) {
        Map<String, Integer> columns = new LinkedHashMap<>();
        for (int index = 0; index < header.size(); index++) {
            String normalized = header.get(index).trim().toLowerCase(Locale.ROOT);
            if (columns.putIfAbsent(normalized, index) != null) {
                return null;
            }
        }
        return columns.keySet().containsAll(REQUIRED_HEADERS) && header.size() == 4
                ? columns
                : null;
    }

    private String value(List<String> row, Map<String, Integer> columns, String column) {
        return row.get(columns.get(column)).trim();
    }

    private List<List<String>> parseCsv(String csv) {
        List<List<String>> rows = new ArrayList<>();
        List<String> row = new ArrayList<>();
        StringBuilder field = new StringBuilder();
        boolean quoted = false;
        boolean afterQuote = false;
        int line = 1;

        for (int index = 0; index < csv.length(); index++) {
            char character = csv.charAt(index);
            if (quoted) {
                if (character == '"') {
                    if (index + 1 < csv.length() && csv.charAt(index + 1) == '"') {
                        field.append('"');
                        index++;
                    } else {
                        quoted = false;
                        afterQuote = true;
                    }
                } else {
                    field.append(character);
                    if (character == '\n') line++;
                }
                continue;
            }

            if (afterQuote && character != ',' && character != '\r' && character != '\n'
                    && !Character.isWhitespace(character)) {
                throw new CsvFormatException("Invalid character after a quoted value near line " + line + ".");
            }
            if (character == '"') {
                if (field.length() != 0 || afterQuote) {
                    throw new CsvFormatException("Unexpected quote near line " + line + ".");
                }
                quoted = true;
            } else if (character == ',') {
                row.add(field.toString());
                field.setLength(0);
                afterQuote = false;
            } else if (character == '\n' || character == '\r') {
                if (character == '\r' && index + 1 < csv.length() && csv.charAt(index + 1) == '\n') {
                    index++;
                }
                row.add(field.toString());
                field.setLength(0);
                afterQuote = false;
                if (!(row.size() == 1 && row.get(0).isBlank())) {
                    rows.add(row);
                }
                row = new ArrayList<>();
                line++;
            } else if (!afterQuote) {
                field.append(character);
            }
        }
        if (quoted) {
            throw new CsvFormatException("Unclosed quoted value near line " + line + ".");
        }
        if (field.length() > 0 || !row.isEmpty()) {
            row.add(field.toString());
            if (!(row.size() == 1 && row.get(0).isBlank())) {
                rows.add(row);
            }
        }
        return rows;
    }

    private record RestaurantKey(String name, String location) {
    }

    private record ImportRow(String restaurantName, String location, String itemName, double price) {
    }

    private static class CsvFormatException extends RuntimeException {
        private CsvFormatException(String message) {
            super(message);
        }
    }
}
