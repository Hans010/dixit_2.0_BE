package com.burnout.dixit.game.domain;

import com.burnout.dixit.common.CardId;
import com.burnout.dixit.common.PlayerId;

import java.util.HashMap;
import java.util.Map;

public class Round {

    private final int roundNumber;
    private final Player storyteller;
    private String clue;
    private final Map<PlayerId, CardId> submissions = new HashMap<>();
    private final Map<PlayerId, CardId> votes = new HashMap<>();
    private final Map<PlayerId, Integer> roundScore = new HashMap<>();

    public boolean noVotesInStorytellerCard = false;

    public Round (int roundNumber, Player storyteller) {
        this.roundNumber = roundNumber;
        this.storyteller = storyteller;
    }

    public Player getStoryteller() { return storyteller; }
    public String clue() { return clue; }
    public Map<PlayerId, CardId> getSubmissions() { return submissions; }
    public Map<PlayerId, CardId> getVotes() { return votes; }
    public Map<PlayerId, Integer> getRoundScore() { return roundScore; }

    public void setClue(String clue) {
        if (this.clue != null) {
            throw new IllegalStateException("Clue already chosen");
        }
        this.clue = clue;
    }

    public void submitCard (PlayerId playerId, CardId cardId) {
        if (submissions.containsKey(playerId)) {
            throw new IllegalStateException("Player already submitted a card");
        }
        if (submissions.containsValue(cardId)) {
            throw new IllegalStateException("Card already submitted by another player");
        }
        submissions.put(playerId, cardId);
    }

    public void submitVote (PlayerId playerId, CardId cardId) {
        if (playerId.equals(this.storyteller.id())) {
            throw new IllegalStateException("Storyteller cannot vote");
        }

        if (votes.containsKey(playerId)) {
            throw new IllegalStateException("Player already voted");
        }

        votes.put(playerId, cardId);
    }

    public boolean allCardsSubmitted(int totalPlayers) {
        return submissions.size() == totalPlayers;
    }

    public boolean allVotesReceived(int totalPlayers) {
        return votes.size() == totalPlayers -1;
    }

    public int getRoundNumber() {
        return roundNumber;
    }

    /**
     * Rehydrates a Round's mutable state from a persisted snapshot (see
     * GameMapper). Bypasses the validation in setClue()/submitCard()/
     * submitVote() (e.g. "clue already chosen") since this is restoring
     * existing state, not making a new move.
     */
    public static Round restore(int roundNumber, Player storyteller, String clue,
                                 Map<PlayerId, CardId> submissions,
                                 Map<PlayerId, CardId> votes,
                                 Map<PlayerId, Integer> roundScore,
                                 boolean noVotesInStorytellerCard) {
        Round round = new Round(roundNumber, storyteller);
        round.clue = clue;
        round.submissions.putAll(submissions);
        round.votes.putAll(votes);
        round.roundScore.putAll(roundScore);
        round.noVotesInStorytellerCard = noVotesInStorytellerCard;
        return round;
    }

}
