package com.burnout.dixit.game.domain.phase;

import com.burnout.dixit.common.CardId;
import com.burnout.dixit.common.PlayerId;

import java.util.HashMap;
import java.util.Map;

public record Voting(
        PlayerId storyteller,
        String clue,
        Map<PlayerId, CardId> submissions,
        Map<PlayerId, PlayerId> votes
        ) implements GamePhase {

    public Voting(PlayerId storyteller, String clue, Map<PlayerId, CardId> submissions) {
        this(storyteller, clue, submissions, new HashMap<>());
    }

    public Voting vote(PlayerId voter, PlayerId votedPlayer, int totalPlayers) {
        if(voter.equals(storyteller)) {
            throw new IllegalStateException("Storyteller cannot vote");
        }

        if(votes.containsKey(voter)) {
            throw new IllegalStateException("Player already voted");
        }

        Map<PlayerId, PlayerId> updated = new HashMap<>(votes);
        updated.put(voter, votedPlayer);

        if (votes.size() == totalPlayers-1) {
//            return new Scoring(storyteller, submissions, votes);
            return null;
        }

        return new Voting(storyteller, clue, submissions, updated);
    }

    @Override
    public PhaseType type() {
        return PhaseType.VOTING;
    }
}
