package com.andrew.hdss.round;

import java.time.LocalDate;
import java.util.List;

public record ValidationReport(
        boolean passed,
        LocalDate checkedOn,
        int checksRun,
        DeltaSummary delta,
        List<CheckFailure> failures
) {
}
