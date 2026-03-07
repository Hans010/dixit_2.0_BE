package com.burnout.dixit.game.domain.phase;

import com.burnout.dixit.common.CardId;
import com.burnout.dixit.common.PlayerId;
import com.burnout.dixit.game.domain.Round;
import rest.PlayerResource;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class Scoring implements GamePhase {

    public void scoreRound(Round round) {
        setStorytellerScore(round);
        scoreVotes(round);
    }

    private void setStorytellerScore(Round round) {
        CardId storytellerCardId = round.getSubmissions().get(round.getStoryteller());
        Map<PlayerId, CardId> votes = round.getVotes();
        if (votes.values().contains(storytellerCardId) && !votes.values().stream().allMatch(cardId -> cardId.equals(storytellerCardId))) {
            round.getRoundScore().put(round.getStoryteller(), 3);
        } else  {
            round.getRoundScore().put(round.getStoryteller(), 0);
        }
    }

    private void scoreVotes(Round round) {
        CardId storytellerCardId = round.getSubmissions().get(round.getStoryteller());
        Map <PlayerId, CardId> votes =  round.getVotes();
        Map<PlayerId, CardId> submissions = round.getSubmissions();
        Map<PlayerId, CardId> otherThanStoryteller = new HashMap<>();
        votes.forEach((playerId, cardId) -> {
            round.getRoundScore().put(playerId, cardId.equals(storytellerCardId) ? 3 : 0);
            if (!cardId.equals(storytellerCardId)) {
                Optional<Map.Entry<PlayerId, CardId>> player = submissions.entrySet().stream().filter(entry -> entry.equals(cardId)).findFirst();
                player.ifPresent(entry -> round.getRoundScore().put(entry.getKey(), round.getRoundScore().get(entry.getKey())+1));
            }
        });





    }




    @Override
    public PhaseType type() {
        return PhaseType.SCORING;
    }
}
