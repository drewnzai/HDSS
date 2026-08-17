package com.andrew.hdss.api;

import com.andrew.hdss.api.swaggerinterfaces.LocationApi;
import com.andrew.hdss.dtos.CreateLocationRequest;
import com.andrew.hdss.dtos.LocationDto;
import com.andrew.hdss.dtos.LocationImportResult;
import com.andrew.hdss.services.LocationService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
@Tag(name= "Location Management")
public class LocationController implements LocationApi {
    private final LocationService locationService;

    @GetMapping
    public List<LocationDto> getChildren(@RequestParam(name = "id", required = false) Long parentId) {
        return locationService.getChildren(parentId);
    }

    @GetMapping("/{id}/descendants")
    public List<LocationDto> getDescendants(@PathVariable(name = "id") Long id) {
        return locationService.getDescendants(id);
    }

    @GetMapping("/{id}/ancestors")
    public List<LocationDto> getAncestors(@PathVariable Long id) {
        return locationService.getAncestors(id);
    }

    @PostMapping
    public ResponseEntity<String> createLocation(@RequestBody CreateLocationRequest request) {
        locationService.createLocation(
                request.getName(), request.getType(), request.getParentId()
        );
        return new ResponseEntity<>("Location Created Successfully", HttpStatus.OK);
    }

    @PostMapping(value = "/import", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public LocationImportResult importLocations(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty.");
        }
        return locationService.importFromSpreadsheet(file);
    }
}
