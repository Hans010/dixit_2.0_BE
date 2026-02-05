package com.burnout.dixit.game.domain.phase;

import com.burnout.dixit.common.PlayerId;

public record StorytellerChoice(PlayerId storyteller) implements GamePhase {

    public CardSubmission chooseClue(String clue) {
        return new CardSubmission(storyteller, clue);
    }

    @Override
    public PhaseType type() {
        return PhaseType.STORYTELLER_CHOICE;
    }
}
