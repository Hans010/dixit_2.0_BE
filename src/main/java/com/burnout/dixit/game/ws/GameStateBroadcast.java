package com.burnout.dixit.game.ws;

import java.util.Map;
import java.util.UUID;

/**
 * The payload pushed to all connected WebSocket clients after every
 * successful game command. A superset of GameResource's GameStateResponse
 * (phase/round/storyteller) plus the scoreboard, so a frontend doesn't
 * need a follow-up REST call after every event just to get current scores.
 *
 * Deliberately does NOT include hands or card images - those are
 * player-specific (each player should only see their own hand) and this
 * broadcast goes to every connected client indiscriminately. Hands stay on
 * GET /player/{playerId}/hand.
 */
public record GameStateBroadcast(
        String phase,
        int roundNumber,
        UUID storytellerId,
        String storytellerName,
        Map<String, Integer> scoreboard
) {
}
