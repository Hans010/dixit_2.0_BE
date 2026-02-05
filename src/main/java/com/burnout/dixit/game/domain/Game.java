package com.burnout.dixit.game.domain;

import com.burnout.dixit.common.GameId;
import com.burnout.dixit.common.PlayerId;
import com.burnout.dixit.game.domain.phase.GamePhase;

import java.time.Instant;
import java.util.List;

public record Game(
        GameId id,
        GamePhase phase,
        List<Player> players,
        int currentRound,
        Instant lastUpdated
) {

    public Game initialGame(GamePhase newPhase) {
        return new Game(id, newPhase, players, 1, Instant.now());
    }

    public Game withPhase(GamePhase newPhase) {
        return new Game(id, newPhase, players, currentRound, Instant.now());
    }

    public Game withRoundNumber(int roundNumber) {
        return new Game(id, phase, players, roundNumber, Instant.now());
    }

    public Player currentStoryteller() {
        return players.get((currentRound -1) % players.size());
    }
}
