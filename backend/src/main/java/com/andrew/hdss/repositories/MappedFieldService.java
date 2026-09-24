package com.andrew.hdss.repositories;

import com.andrew.hdss.dtos.MappableFieldDto;
import com.andrew.hdss.models.enums.MappedEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class MappedFieldService {

    private static final Map<MappedEntity, List<MappableFieldDto>> FIELDS_BY_ENTITY = Map.of(
            MappedEntity.HOUSEHOLD, List.of(
                    new MappableFieldDto("householdCode", "Household code"),
                    new MappableFieldDto("locationId", "Location"),
                    new MappableFieldDto("latitude", "Latitude"),
                    new MappableFieldDto("longitude", "Longitude")
            ),
            MappedEntity.INDIVIDUAL, List.of(
                    new MappableFieldDto("firstName", "First name"),
                    new MappableFieldDto("lastName", "Last name"),
                    new MappableFieldDto("sex", "Sex"),
                    new MappableFieldDto("dateOfBirth", "Date of birth"),
                    new MappableFieldDto("dobEstimated", "Date of birth estimated (flag)"),
                    new MappableFieldDto("motherClientId", "Mother"),
                    new MappableFieldDto("fatherClientId", "Father")
            ),
            MappedEntity.MEMBERSHIP, List.of(
                    new MappableFieldDto("relationshipToHead", "Relationship to head"),
                    new MappableFieldDto("startType", "Membership start type"),
                    new MappableFieldDto("startDate", "Membership start date"),
                    new MappableFieldDto("endDate", "Membership end date"),
                    new MappableFieldDto("endType", "Membership end type")
            )
    );

    public List<MappableFieldDto> getMappableFields(MappedEntity entity) {
        if (entity == MappedEntity.NONE) {
            return List.of();
        }

        List<MappableFieldDto> fields = FIELDS_BY_ENTITY.get(entity);
        if (fields == null) {
            throw new IllegalArgumentException("No mappable fields defined for entity: " + entity);
        }

        return fields;
    }
}
