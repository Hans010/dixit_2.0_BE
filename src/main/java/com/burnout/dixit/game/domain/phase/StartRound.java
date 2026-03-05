package com.burnout.dixit.game.domain.phase;

public record StartRound() implements GamePhase {

    @Override
    public PhaseType type() {
        return PhaseType.START_ROUND;
    }
}
