package com.burnout.dixit.game.domain.phase;

import com.burnout.dixit.common.CardId;
import com.burnout.dixit.common.PlayerId;

import java.util.HashMap;
import java.util.Map;

public record CardSubmission(
        PlayerId storyteller,
        String clue,
        Map<PlayerId, CardId> submissions
) implements GamePhase {

    public CardSubmission(PlayerId storyteller, String clue) {
        this(storyteller, clue, new HashMap<>());
    }

    public CardSubmission submit(PlayerId playerId, CardId cardId, int totalPlayers) {
        if (playerId.equals(storyteller)) {
            throw new IllegalStateException("Storyteller cannot submit");
        }

        if (submissions.containsKey(playerId)) {
            throw new IllegalStateException("Player already submitted");
        }

        Map<PlayerId, CardId> updated = new HashMap<>(submissions);
        updated.put(playerId, cardId);

        if (submissions.size() == totalPlayers -1) {
//            return new Voting(storyteller, clue, Map.copyOf(updated));
            return null;
        }

        return new CardSubmission(storyteller, clue, Map.copyOf(updated));
    }

    @Override
    public PhaseType type() {
        return PhaseType.CARD_SUBMISSION;
    }
}
