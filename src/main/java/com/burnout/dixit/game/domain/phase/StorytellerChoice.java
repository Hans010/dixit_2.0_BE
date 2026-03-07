package com.burnout.dixit.game.domain.phase;

import com.burnout.dixit.common.CardId;
import com.burnout.dixit.common.PlayerId;
import com.burnout.dixit.game.domain.Round;

public final class StorytellerChoice implements GamePhase {

    public StorytellerChoice() {
    }

    public void setClue(Round round, String clue) {
        round.setClue(clue);
    }

    public void submitStorytellerCard(Round round, CardId storytellerCardId) {

        if (!round.getSubmissions().isEmpty()) {
            round.getSubmissions().clear();
        }
        round.submitCard(round.getStoryteller(), storytellerCardId);
    }

    @Override
    public PhaseType type() {
        return PhaseType.STORYTELLER_CHOICE;
    }
}
