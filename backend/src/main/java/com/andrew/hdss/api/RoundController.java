package com.andrew.hdss.api;

import com.andrew.hdss.round.RoundValidator;
import com.andrew.hdss.round.ValidationReport;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/rounds")
@RequiredArgsConstructor
@Tag(name = "Round Creation")
public class RoundController {

    private final RoundValidator roundValidator;

    // Dry run. Runs every check against a consistent snapshot of live and
    // reports. Writes nothing anywhere.
    @PostMapping("/validate")
    public ValidationReport validate() {
        return roundValidator.validate();
    }
}
