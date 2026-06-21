package com.burnout.dixit.game.persistence;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A plain-data snapshot of a Game, suitable for JSON serialization to
 * Redis. Deliberately decoupled from the live domain model (Game, Round,
 * GamePhase, Deck, ...) rather than serializing those classes directly -
 * several of them aren't Jackson-friendly out of the box (a sealed
 * interface with mostly-stateless implementations, record-keyed maps,
 * an ArrayDeque), and a dedicated DTO keeps the wire format stable even
 * if the domain model's internals change later.
 *
 * Game.toSnapshot() / Game.fromSnapshot() convert between this and the
 * live domain object.
 */
public record GameSnapshot(
        UUID gameId,
        String phaseType,
        Integer lobbyMinPlayers,
        Integer lobbyMaxPlayers,
        List<PlayerSnapshot> players,
        int roundCounter,
        RoundSnapshot currentRound,
        Map<UUID, Integer> scoreboard,
        DeckSnapshot deck,
        Map<UUID, List<UUID>> hands,
        Instant lastUpdated
) {

    public record PlayerSnapshot(UUID id, String name) {
    }

    public record RoundSnapshot(
            int roundNumber,
            UUID storytellerId,
            String clue,
            Map<UUID, UUID> submissions,
            Map<UUID, UUID> votes,
            Map<UUID, Integer> roundScore,
            boolean noVotesInStorytellerCard
    ) {
    }

    public record CardSnapshot(UUID id, String filename) {
    }

    public record DeckSnapshot(
            List<CardSnapshot> drawPile,
            List<CardSnapshot> allCards
    ) {
    }
}
