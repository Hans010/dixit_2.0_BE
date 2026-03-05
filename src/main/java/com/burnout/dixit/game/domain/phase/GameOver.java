package com.burnout.dixit.game.domain.phase;

public record GameOver() implements GamePhase {

    @Override
    public PhaseType type() {
        return PhaseType.GAMEOVER;
    }
}
