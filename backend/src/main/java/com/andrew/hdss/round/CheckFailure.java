package com.andrew.hdss.round;

import java.util.List;

public record CheckFailure(
        String id,
        String description,
        int count,
        boolean countIsLowerBound,
        List<Violation> samples
) {
}
