package com.andrew.hdss.dtos.sync;

import java.util.List;

public record SyncPushResponse(
        List<SyncItemResult> households,
        List<SyncItemResult> individuals,
        List<SyncItemResult> memberships,
        List<SyncItemResult> visitResults,
        List<SyncItemResult> formResponseResults,
        List<SyncItemResult> answerResults
) {}