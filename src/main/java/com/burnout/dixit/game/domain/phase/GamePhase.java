package com.burnout.dixit.game.domain.phase;

public sealed interface GamePhase permits Lobby, StartRound, StorytellerChoice, CardSubmission, Voting, Scoring, GameOver {
    PhaseType type();
}

