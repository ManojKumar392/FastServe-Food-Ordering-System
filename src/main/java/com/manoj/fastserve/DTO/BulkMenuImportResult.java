package com.manoj.fastserve.DTO;

import java.util.List;

public record BulkMenuImportResult(
        boolean successful,
        int restaurantsCreated,
        int menuItemsCreated,
        List<String> errors
) {
    public static BulkMenuImportResult success(int restaurantsCreated, int menuItemsCreated) {
        return new BulkMenuImportResult(true, restaurantsCreated, menuItemsCreated, List.of());
    }

    public static BulkMenuImportResult failure(List<String> errors) {
        return new BulkMenuImportResult(false, 0, 0, List.copyOf(errors));
    }
}
