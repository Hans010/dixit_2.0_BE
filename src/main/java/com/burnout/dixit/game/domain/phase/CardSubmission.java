package com.burnout.dixit.game.domain.phase;

import com.burnout.dixit.common.CardId;
import com.burnout.dixit.common.PlayerId;
import com.burnout.dixit.game.domain.Round;

public final class CardSubmission implements GamePhase {

    public CardSubmission() {
    }

    public void submit(Round round, PlayerId playerId, CardId cardId) {
        round.submitCard(playerId, cardId);
    }

    public boolean allCardsSubmitted(Round round, int totalPlayers) {
        return round.getSubmissions().size() == totalPlayers;
    }

@Override
public PhaseType type() {
    return PhaseType.CARD_SUBMISSION;
}
}
