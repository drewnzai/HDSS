package com.andrew.hdss.dtos;

import java.util.List;

public record ReorderQuestionsRequest(
        List<Long> questionIdsInOrder
) {}