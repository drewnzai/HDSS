package com.andrew.hdss.api;

import com.andrew.hdss.dtos.MappableFieldDto;
import com.andrew.hdss.models.enums.MappedEntity;
import com.andrew.hdss.repositories.MappedFieldService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mapped-fields")
@RequiredArgsConstructor
@Tag(name = "Mapped Field Metadata")
public class MappedFieldController {

    private final MappedFieldService mappedFieldService;

    // Spring binds the path segment to the MappedEntity enum directly
    // (case-sensitive match against the constant name — "HOUSEHOLD",
    // "INDIVIDUAL", "MEMBERSHIP", "NONE").
    @GetMapping("/{entity}")
    public List<MappableFieldDto> getMappableFields(@PathVariable MappedEntity entity) {
        return mappedFieldService.getMappableFields(entity);
    }
}
