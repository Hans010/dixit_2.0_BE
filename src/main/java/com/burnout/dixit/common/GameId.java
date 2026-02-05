package com.burnout.dixit.common;

import java.util.UUID;

public record GameId(UUID uuid) {
    public static GameId newId() {
        return new GameId(UUID.randomUUID());
    }
}
