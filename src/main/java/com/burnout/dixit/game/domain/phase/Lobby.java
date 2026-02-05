package com.burnout.dixit.game.domain.phase;

import com.burnout.dixit.common.PlayerId;

public record Lobby (int minPlayers, int maxPlayers) implements GamePhase {

    public StorytellerChoice startGame(PlayerId storyteller) {
        return new StorytellerChoice(storyteller);
    }

    @Override
    public PhaseType type() {
        return PhaseType.LOBBY;
    }
}
