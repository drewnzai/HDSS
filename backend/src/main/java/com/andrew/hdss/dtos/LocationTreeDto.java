package com.andrew.hdss.dtos;

import com.andrew.hdss.models.Location;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationTreeDto {
    private Long id;
    private String name;
    private String type;
    List<LocationTreeDto> children;

    public static LocationTreeDto from(Location location) {
        return new LocationTreeDto(
                location.getId(),
                location.getName(),
                location.getType().name(),
                location.getChildren().stream().map(LocationTreeDto::from).toList()
        );
    }
}
