package com.burnout.dixit.game.ws;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.websocket.OnClose;
import jakarta.websocket.OnError;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
import jakarta.websocket.server.ServerEndpoint;
import org.jboss.logging.Logger;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Push-only WebSocket endpoint for real-time game state updates.
 *
 * Clients connect here purely to *receive* broadcasts - all game actions
 * (add player, start game, submit card, vote, etc.) still go through the
 * REST API in GameResource/PlayerResource exactly as before. This endpoint
 * never reads or acts on incoming client messages.
 *
 * Models a single shared game (matching GameService's current single-game
 * scope): every connected client receives every broadcast, with no
 * per-game or per-player filtering. Once multi-game support exists, this
 * would need to scope broadcasts by gameId (e.g. via a path param like
 * /ws/game/{gameId}) instead of broadcasting to every open connection.
 */
@ServerEndpoint("/ws/game")
@ApplicationScoped
public class GameSocket {

    private static final Logger LOG = Logger.getLogger(GameSocket.class);

    private final Set<Session> sessions = ConcurrentHashMap.newKeySet();

    @OnOpen
    public void onOpen(Session session) {
        sessions.add(session);
        LOG.infof("WebSocket client connected: %s (total: %d)", session.getId(), sessions.size());
    }

    @OnClose
    public void onClose(Session session) {
        sessions.remove(session);
        LOG.infof("WebSocket client disconnected: %s (total: %d)", session.getId(), sessions.size());
    }

    @OnError
    public void onError(Session session, Throwable throwable) {
        sessions.remove(session);
        LOG.warnf("WebSocket error on session %s: %s", session.getId(), throwable.getMessage());
    }

    /**
     * Sends the given payload (already-serialized JSON, see
     * GameStateBroadcast) to every connected client. Called by GameService
     * after every successful command, so all connected clients stay in
     * sync without needing to poll GET /game/state.
     *
     * Dead/closed sessions are skipped defensively - onClose/onError
     * should already remove them, but a send to a stale session shouldn't
     * be allowed to break the broadcast for everyone else.
     */
    public void broadcast(String json) {
        for (Session session : sessions) {
            if (!session.isOpen()) {
                continue;
            }
            session.getAsyncRemote().sendText(json, result -> {
                if (result.getException() != null) {
                    LOG.warnf("Failed to send to session %s: %s", session.getId(), result.getException().getMessage());
                }
            });
        }
    }
}
