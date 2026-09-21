package com.andrew.hdss.api;

import com.andrew.hdss.api.swaggerinterfaces.VisitApi;
import com.andrew.hdss.dtos.CreateVisitRequest;
import com.andrew.hdss.dtos.UpdateVisitStatusRequest;
import com.andrew.hdss.dtos.VisitDto;
import com.andrew.hdss.services.VisitService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/visits")
@RequiredArgsConstructor
@Tag(name = "Visit Management")
public class VisitController implements VisitApi {

    private final VisitService visitService;

    @GetMapping
    public List<VisitDto> getAllVisits() {
        return visitService.getAllVisits();
    }

    @GetMapping("/{id}")
    public VisitDto getVisit(@PathVariable Long id) {
        return visitService.getVisit(id);
    }

    @PostMapping
    public VisitDto createVisit(
            @RequestBody CreateVisitRequest request
    ) {
        return visitService.createVisit(request);
    }

    @PutMapping("/{id}/status")
    public VisitDto updateStatus(@PathVariable Long id, @RequestBody UpdateVisitStatusRequest request) {
        return visitService.updateStatus(id, request.status());
    }
}
