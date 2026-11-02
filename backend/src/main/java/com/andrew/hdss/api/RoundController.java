package com.andrew.hdss.api;

import com.andrew.hdss.api.swagger.RoundApi;
import com.andrew.hdss.dtos.CreateRoundRequest;
import com.andrew.hdss.dtos.RoundCreationResult;
import com.andrew.hdss.dtos.RoundDetailsDto;
import com.andrew.hdss.dtos.RoundDto;
import com.andrew.hdss.round.ValidationReport;
import com.andrew.hdss.services.RoundService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rounds")
@RequiredArgsConstructor
@Tag(name = "Round Management")
public class RoundController implements RoundApi {

    private final RoundService roundService;

    @PostMapping("/validate")
    public ResponseEntity<ValidationReport> validate() {
        return ResponseEntity.ok(roundService.validate());
    }

    @PostMapping
    public ResponseEntity<?> createRound(
            @Valid @RequestBody CreateRoundRequest request
    ) {
        RoundCreationResult result =
                roundService.createRound(request.roundName());

        if (!result.started()) {
            return ResponseEntity
                    .status(HttpStatus.UNPROCESSABLE_ENTITY)
                    .body(result.validationReport());
        }

        return ResponseEntity
                .accepted()
                .body(result.round());
    }

    @GetMapping
    public ResponseEntity<List<RoundDto>> getRounds() {
        return ResponseEntity.ok(roundService.getRounds());
    }

    @GetMapping("/{id}")
    public ResponseEntity<RoundDetailsDto> getRound(
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(roundService.getRound(id));
    }
}