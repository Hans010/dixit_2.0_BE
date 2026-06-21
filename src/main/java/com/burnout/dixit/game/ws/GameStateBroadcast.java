package com.burnout.dixit.game.ws;

import java.util.Map;
import java.util.UUID;

/**
 * The payload pushed to all connected WebSocket clients after every
 * successful game command. A superset of GameResource's GameStateResponse
 * (phase/round/storyteller) plus the scoreboard, current round's clue, and
 * submission/vote progress, so a frontend doesn't need a follow-up REST
 * call after every event just to keep its UI in sync.
 *
 * Several fields are deliberately phase-gated rather than always present,
 * to avoid leaking information the real game keeps secret at that point:
 *
 * - clue: null until the storyteller has chosen one for the current round
 *   (i.e. during Lobby/StorytellerChoice before ChooseClue fires).
 * - storytellerCard: null until the round is scored. Revealing this any
 *   earlier (e.g. during Voting) would let a client work out which
 *   submission belongs to the storyteller before votes are cast, which
 *   defeats the core guessing mechanic of the game.
 *
 * Deliberately does NOT include hands - those are player-specific (each
 * player should only see their own hand) and this broadcast goes to every
 * connected client indiscriminately. Hands stay on
 * GET /player/{playerId}/hand.
 */
public record GameStateBroadcast(
        String phase,
        int roundNumber,
        UUID storytellerId,
        String storytellerName,
        String clue,
        SubmissionProgress submissions,
        VoteProgress votes,
        CardView storytellerCard,
        Map<String, Integer> scoreboard
) {

    public record SubmissionProgress(int received, int expected) {
    }

    public record VoteProgress(int received, int expected) {
    }

    public record CardView(UUID cardId, String imageUrl) {
    }
}
