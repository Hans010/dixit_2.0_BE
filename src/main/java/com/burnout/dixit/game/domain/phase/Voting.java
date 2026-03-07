package com.burnout.dixit.game.domain.phase;

import com.burnout.dixit.common.CardId;
import com.burnout.dixit.common.PlayerId;
import com.burnout.dixit.game.domain.Round;

import java.util.HashMap;
import java.util.Map;

public final class Voting implements GamePhase {

    public Voting() {
    }

    public void vote(Round round, PlayerId playerId, CardId votedCardId) {
        if (round.getStoryteller().equals(playerId)) {
            throw new IllegalStateException("Storyteller cannot vote");
        }

        if (round.getVotes().containsKey(playerId)) {
            throw new IllegalStateException("Player already voted");
        }

        round.submitVote(playerId, votedCardId);
    }

    public boolean allVotesSubmitted(Round round, int totalPlayers) {
        return round.getVotes().size() == totalPlayers -1;
    }


    @Override
    public PhaseType type() {
        return PhaseType.VOTING;
    }
}
