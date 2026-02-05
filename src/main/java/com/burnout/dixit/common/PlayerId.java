package com.burnout.dixit.common;

import java.util.UUID;

public record PlayerId(UUID uuid) {
    public static PlayerId newId() {
        return new PlayerId(UUID.randomUUID());
    }
}
