package com.andrew.hdss.api;

import com.andrew.hdss.dtos.CreateLocationRequest;
import com.andrew.hdss.dtos.LocationDto;
import com.andrew.hdss.services.LocationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/locations")
@RequiredArgsConstructor
public class LocationController {
    private final LocationService locationService;

    @GetMapping
    public List<LocationDto> getChildren(@RequestParam(required = false) Long parentId) {
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
}
