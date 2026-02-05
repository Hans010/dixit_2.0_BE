package com.burnout.dixit.game.domain.phase;

import com.burnout.dixit.common.CardId;
import com.burnout.dixit.common.PlayerId;

import java.util.Map;

public record Scoring(
        PlayerId storyteller,
        Map<PlayerId, CardId> submissions,
        Map<PlayerId, PlayerId> votes
) implements GamePhase {

    public StorytellerChoice nextRound(PlayerId nextStoryteller) {
        return new StorytellerChoice(nextStoryteller);
    }

    @Override
    public PhaseType type() {
        return PhaseType.SCORING;
    }
}
