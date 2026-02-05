package com.burnout.dixit.game.domain.phase;

public sealed interface GamePhase permits Lobby, StorytellerChoice, CardSubmission, Voting, Scoring {
    PhaseType type();
}

