package com.andrew.hdss.dtos;

import com.andrew.hdss.models.enums.VisitStatus;

public record UpdateVisitStatusRequest(VisitStatus status) {
}
