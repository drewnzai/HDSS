package com.andrew.hdss.services;

import com.andrew.hdss.dtos.LocationDto;
import com.andrew.hdss.exceptions.EntityNotFoundException;
import com.andrew.hdss.models.Location;
import com.andrew.hdss.models.enums.LocationType;
import com.andrew.hdss.repositories.LocationRepository;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class LocationService {
    private LocationRepository locationRepository;

    @Transactional
    public void createLocation(String name, String locationType, Long parentId) {
        Location parent = null;

        LocationType type = LocationType.valueOf(locationType);

        if (type == LocationType.COUNTRY) {
            if (parentId != null) {
                throw new IllegalArgumentException("A country cannot have a parent.");
            }
        } else {
            if (parentId == null) {
                throw new IllegalArgumentException("A parent is required for type " + type);
            }
            parent = locationRepository.findById(parentId)
                    .orElseThrow(() -> new EntityNotFoundException("Parent location not found: " + parentId));

            LocationType expectedParentType = type.parentType();
            if (parent.getType() != expectedParentType) {
                throw new IllegalArgumentException(
                        "A " + type + " must have a parent of type " + expectedParentType +
                                ", but parent " + parent.getId() + " is a " + parent.getType()
                );
            }
        }

        if (locationRepository.existsByParentIdAndName(parentId, name)) {
            throw new IllegalArgumentException("A " + type + " named '" + name + "' already exists under this parent.");
        }

        Location location = new Location();
        location.setName(name);
        location.setType(type);
        location.setParent(parent);
        location.setAncestorPath(buildAncestorPath(parent));

        locationRepository.save(location);
    }

    @Transactional(readOnly = true)
    @Cacheable(
            value = "location-descendants",
            key = "#locationId"
    )
    public List<LocationDto> getDescendants(Long locationId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new EntityNotFoundException("Location not found: " + locationId));
        String prefix = location.getAncestorPath() + location.getId() + "/";

        return locationRepository.findAllDescendants(prefix)
                .stream()
                .map(LocationDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(
            value = "location-children",
            key = "#locationId"
    )
    public List<LocationDto> getChildren(Long parentId) {
        List<Location> children;

        if(parentId == null){
           children = locationRepository.findByParentIsNull();
        }else{
            children = locationRepository.findByParentId(parentId);
        }

        return children.stream()
                .map(LocationDto::from)
                .toList();
    }

    @Transactional(readOnly = true)
    @Cacheable(
            value = "location-ancestors",
            key = "#locationId"
    )
    public List<LocationDto> getAncestors(Long locationId) {
        Location location = locationRepository.findById(locationId)
                .orElseThrow(() -> new EntityNotFoundException("Location not found: " + locationId));

        if (location.getAncestorPath().isEmpty()) return List.of();

        List<Long> ancestorIds = Arrays.stream(location.getAncestorPath().split("/"))
                .map(Long::parseLong)
                .collect(Collectors.toList());

        return locationRepository.findAllById(ancestorIds)
                .stream()
                .map(LocationDto::from)
                .toList();
    }

    private String buildAncestorPath(Location parent) {
        if (parent == null) return "";
        return parent.getAncestorPath() + parent.getId() + "/";
    }
}
