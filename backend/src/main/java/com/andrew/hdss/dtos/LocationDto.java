package com.andrew.hdss.dtos;

import com.andrew.hdss.models.Location;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocationDto {
    private Long id;
    private String name;
    private String type;
    private Long parentId;
    private String code;

    public static LocationDto from(Location location) {
        return new LocationDto(
                location.getId(),
                location.getName(),
                location.getType().name(),
                location.getParent() != null ? location.getParent().getId() : null,
                location.getCode()
        );
    }
}
