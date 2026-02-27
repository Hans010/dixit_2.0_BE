package com.burnout.dixit.game.domain;

import com.burnout.dixit.common.GameId;
import com.burnout.dixit.common.PlayerId;
import com.burnout.dixit.game.domain.phase.GamePhase;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

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

    public Game addPlayer(String name) {
        List<Player> currentPlayers = players;
        Player player = new Player(PlayerId.newId(), name);
        currentPlayers.add(player);
        return new Game(id, phase, currentPlayers, currentRound, Instant.now());
    }

    public Game withPhase(GamePhase newPhase) {
        return new Game(id, newPhase, players, currentRound, Instant.now());
    }

    public Player currentStoryteller() {
        System.out.println("current round " + currentRound);
        return !players.isEmpty() ? players.get((currentRound -1) % players.size()) : null;
    }
}
