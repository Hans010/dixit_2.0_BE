package com.burnout.dixit.game.domain;

import com.burnout.dixit.common.PlayerId;

import java.util.HashMap;
import java.util.Map;

public class Round {

    private final int number;
    private final PlayerId storyteller;

    private String clue;

    private final Map<PlayerId, String> submissions = new HashMap<>();
    private final Map<PlayerId, String> votes = new HashMap<>();

    public Round (int number, PlayerId storyteller) {
        this.number = number;
        this.storyteller = storyteller;
    }

    public int number() { return number; }
    public PlayerId storyteller() { return storyteller; }

    public void setClue(String clue) {
        if (this.clue != null) {
            throw new IllegalStateException("Clue already chosen");
        }
        this.clue = clue;
    }

    public String clue() { return clue; }

    public void submitCard (PlayerId playerId, String cardId) {
        if (submissions.containsKey(playerId)) {
            throw new IllegalStateException("Player already submitted card");
        }
        submissions.put(playerId, cardId);
    }

    public void submitVote (PlayerId playerId, String cardId) {
        if (playerId.equals(this.storyteller)) {
            throw new IllegalStateException("Storyteller cannot vote");
        }

        if (votes.containsKey(playerId)) {
            throw new IllegalStateException("Player already voted");
        }

        votes.put(playerId, cardId);
    }

    public boolean allCardsSubmitted(int totalPlayers) {
        return submissions.size() == totalPlayers -1;
    }

    public boolean allVotesReceived(int totalPlayers) {
        return votes.size() == totalPlayers -1;
    }




}
