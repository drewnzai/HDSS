package com.andrew.hdss.dtos;

import com.andrew.hdss.models.enums.LocationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationImportResult {
    private int rowsProcessed;
    private Map<LocationType, Integer> created;
    private Map<LocationType, Integer> reused;
    private List<String> errors;
}
