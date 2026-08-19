package com.andrew.hdss.dtos.sync;

import com.andrew.hdss.dtos.sync.enums.SyncStatus;

public record SyncItemResult(
        String clientId,
        Long id,
        SyncStatus status,
        String message
) {
    public static SyncItemResult created(String clientId, Long id) {
        return new SyncItemResult(clientId, id, SyncStatus.CREATED, null);
    }
    public static SyncItemResult updated(String clientId, Long id) {
        return new SyncItemResult(clientId, id, SyncStatus.UPDATED, null);
    }
    public static SyncItemResult error(String clientId, String message) {
        return new SyncItemResult(clientId, null, SyncStatus.ERROR, message);
    }
}