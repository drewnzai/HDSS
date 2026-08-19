package com.andrew.hdss.dtos.sync;

public record UpsertResult<T>(T entity, boolean wasNew) {}