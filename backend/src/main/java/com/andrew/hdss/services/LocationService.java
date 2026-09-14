package com.andrew.hdss.services;

import com.andrew.hdss.dtos.LocationDto;
import com.andrew.hdss.dtos.LocationImportResult;
import com.andrew.hdss.exceptions.EntityNotFoundException;
import com.andrew.hdss.models.Location;
import com.andrew.hdss.models.enums.LocationType;
import com.andrew.hdss.repositories.LocationRepository;
import lombok.AllArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
@Transactional
public class LocationService {
    private LocationRepository locationRepository;

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "location-children", allEntries = true),
            @CacheEvict(cacheNames = "location-descendants", allEntries = true),
            @CacheEvict(cacheNames = "location-ancestors", allEntries = true)
    })
    public Location createLocation(String name, String locationType, Long parentId, String code) {
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
        location.setCode(code);
        location.setParent(parent);
        location.setAncestorPath(buildAncestorPath(parent));

        return locationRepository.save(location);
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
            key = "#parentId != null ? #parentId : 'ROOT'"
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

    @Transactional
    @Caching(evict = {
            @CacheEvict(cacheNames = "location-children", allEntries = true),
            @CacheEvict(cacheNames = "location-descendants", allEntries = true),
            @CacheEvict(cacheNames = "location-ancestors", allEntries = true)
    })
    public LocationImportResult importFromSpreadsheet(MultipartFile file) throws IOException {
        Map<LocationType, Integer> created = new EnumMap<>(LocationType.class);
        Map<LocationType, Integer> reused = new EnumMap<>(LocationType.class);
        for (LocationType type : LocationType.values()) {
            created.put(type, 0);
            reused.put(type, 0);
        }
        List<String> errors = new ArrayList<>();

        // Cache within this import run so repeated parent chains across rows
        // (e.g. the same SubCounty appearing on 40 rows) don't hit the DB 40 times.
        Map<String, Location> cache = new HashMap<>();
        int rowsProcessed = 0;

        try (Workbook workbook = new XSSFWorkbook(file.getInputStream())) {
            Sheet sheet = workbook.getSheet("Locations");
            if (sheet == null) {
                sheet = workbook.getSheetAt(0); // fall back to the first sheet
            }

            // Column order matches the template: Country, CountryCode, County, CountyCode,
            // SubCounty, SubCountyCode, Location, LocationCode,
            // SubLocation, SubLocationCode
            LocationType[] levels = {
                    LocationType.COUNTRY, LocationType.COUNTY, LocationType.SUB_COUNTY, LocationType.LOCATION, LocationType.SUB_LOCATION
            };

            for (Row row : sheet) {
                if (row.getRowNum() == 0) continue; // header
                if (isRowBlank(row)) continue;

                rowsProcessed++;
                int excelRowNum = row.getRowNum() + 1; // 1-indexed, matches what a user sees in Excel

                try {
                    Location parent = null;
                    boolean chainBroken = false;

                    for (int i = 0; i < levels.length; i++) {
                        String name = getCellString(row, i * 2);
                        String code = getCellString(row, i * 2 + 1);

                        if (name == null || name.isBlank()) {
                            chainBroken = true; // once a level is blank, deeper levels must be too
                            continue;
                        }
                        if (chainBroken) {
                            throw new IllegalArgumentException(
                                    "Row " + excelRowNum + ": " + levels[i] + " ('" + name +
                                            "') given after a blank ancestor level."
                            );
                        }

                        String cacheKey = levels[i] + ":" + (parent != null ? parent.getId() : "root") + ":" + name;
                        Location existing = cache.get(cacheKey);

                        if (existing == null) {
                            Long parentId = parent != null ? parent.getId() : null;
                            existing = locationRepository.findByParentIdAndName(parentId, name).orElse(null);

                            if (existing != null) {
                                reused.merge(levels[i], 1, Integer::sum);
                            } else {
                                existing = createLocation(name, levels[i].name(), parentId, code);
                                created.merge(levels[i], 1, Integer::sum);
                            }
                            cache.put(cacheKey, existing);
                        }

                        parent = existing;
                    }
                } catch (Exception e) {
                    errors.add("Row " + excelRowNum + ": " + e.getMessage());
                }
            }
        }

        return new LocationImportResult(rowsProcessed, created, reused, errors);
    }

    private boolean isRowBlank(Row row) {
        for (int i = 0; i < 12; i++) {
            String value = getCellString(row, i);
            if (value != null && !value.isBlank()) return false;
        }
        return true;
    }

    private String getCellString(Row row, int columnIndex) {
        Cell cell = row.getCell(columnIndex, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        if (cell == null) return null;
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue()); // handles codes read as numbers
            case BLANK -> null;
            default -> cell.toString().trim();
        };
    }

    private String buildAncestorPath(Location parent) {
        if (parent == null) return "";
        return parent.getAncestorPath() + parent.getId() + "/";
    }
}
