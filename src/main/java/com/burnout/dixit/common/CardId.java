package com.burnout.dixit.common;

import java.util.UUID;

public record CardId(UUID uuid) {
    public static CardId newId() {
        return new CardId(UUID.randomUUID());
    }
}
