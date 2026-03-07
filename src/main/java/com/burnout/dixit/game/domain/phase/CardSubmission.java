package com.burnout.dixit.game.domain.phase;

import com.burnout.dixit.common.CardId;
import com.burnout.dixit.common.PlayerId;
import com.burnout.dixit.game.domain.Round;

import java.util.HashMap;
import java.util.Map;

public final class CardSubmission implements GamePhase {

    public CardSubmission() {
    }

    public void submit(Round round, PlayerId playerId, CardId cardId) {

        if (round.getSubmissions().containsKey(playerId)) {
            throw new IllegalStateException("Player already submitted");
        }

        round.getSubmissions().put(playerId, cardId);
    }

    public boolean allCardsSubmitted(Round round, int totalPlayers) {
        return round.getSubmissions().size() == totalPlayers -1;
    }

@Override
public PhaseType type() {
    return PhaseType.CARD_SUBMISSION;
}
}
