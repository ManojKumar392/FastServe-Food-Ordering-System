package com.manoj.fastserve.Controller;

import com.manoj.fastserve.DTO.BulkMenuImportResult;
import com.manoj.fastserve.Service.BulkMenuImportService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/admin/import")
public class AdminImportController {

    private final BulkMenuImportService bulkMenuImportService;

    public AdminImportController(BulkMenuImportService bulkMenuImportService) {
        this.bulkMenuImportService = bulkMenuImportService;
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping(value = "/menu-csv", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BulkMenuImportResult> importMenuCsv(
            @RequestPart("file") MultipartFile file) throws IOException {
        BulkMenuImportResult result = bulkMenuImportService.importCsv(file.getBytes());
        return result.successful()
                ? ResponseEntity.ok(result)
                : ResponseEntity.badRequest().body(result);
    }
}
