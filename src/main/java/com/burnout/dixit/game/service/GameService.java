package com.burnout.dixit.game.service;

import com.burnout.dixit.common.CardId;
import com.burnout.dixit.common.GameId;
import com.burnout.dixit.common.PlayerId;
import com.burnout.dixit.game.command.*;
import com.burnout.dixit.game.domain.Game;
import com.burnout.dixit.game.domain.Player;
import com.burnout.dixit.game.domain.Round;
import com.burnout.dixit.game.domain.phase.GamePhase;
import com.burnout.dixit.game.domain.phase.Lobby;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;
import org.jboss.logging.MDC;

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class GameService {

    private static final Logger LOG = Logger.getLogger(GameService.class);

    GamePhase lobby = new Lobby(3,5);

    Game game = new Game(new GameId(UUID.randomUUID()), lobby,new ArrayList<Player>());

    public String addPlayer(String name) {
        game.addPlayer(name);
        logAction("PLAYER_ADDED", Map.of("playerName", name));
        return name + " has been added";
    }

    public Game getGame() {
        return game;
    }

    public boolean removePlayer(String name) {
        boolean removed = game.removePlayer(name);
        logAction(removed ? "PLAYER_REMOVED" : "PLAYER_REMOVE_FAILED", Map.of("playerName", name));
        return removed;
    }

    public void startGame() {
        try {
            game.handle(new StartGame());
            logAction("GAME_STARTED", Map.of());
        } catch (RuntimeException e) {
            logActionFailed("GAME_START_FAILED", e);
            throw e;
        }
    }

    public void chooseClue(PlayerId storytellerId, String clue, CardId cardId) {
        try {
            game.handle(new ChooseClue(clue, storytellerId, cardId));
            logAction("CLUE_CHOSEN", Map.of(
                    "playerId", storytellerId.uuid().toString(),
                    "cardId", cardId.uuid().toString()
            ));
        } catch (RuntimeException e) {
            logActionFailed("CLUE_CHOOSE_FAILED", e);
            throw e;
        }
    }

    public void submitCard(PlayerId playerId, CardId cardId) {
        try {
            game.handle(new SubmitCard(playerId, cardId));
            logAction("CARD_SUBMITTED", Map.of(
                    "playerId", playerId.uuid().toString(),
                    "cardId", cardId.uuid().toString()
            ));
        } catch (RuntimeException e) {
            logActionFailed("CARD_SUBMIT_FAILED", e);
            throw e;
        }
    }

    public void voteCard(PlayerId playerId, CardId votedCardId) {
        try {
            game.handle(new VoteCard(playerId, votedCardId));
            logAction("VOTE_CAST", Map.of(
                    "playerId", playerId.uuid().toString(),
                    "votedCardId", votedCardId.uuid().toString()
            ));
        } catch (RuntimeException e) {
            logActionFailed("VOTE_FAILED", e);
            throw e;
        }
    }

    public void scoreRound() {
        try {
            game.handle(new ScoreRound());
            logAction("ROUND_SCORED", Map.of());
        } catch (RuntimeException e) {
            logActionFailed("SCORE_ROUND_FAILED", e);
            throw e;
        }
    }

    public String getCurrentPhase() {
        return game.getPhase().type().name();
    }

    public Player getStoryteller() {
        Round round = game.getCurrentRound();
        return round != null ? round.getStoryteller() : null;
    }

    public int getRoundNumber() {
        return game.getRoundCounter();
    }

    public Map<Player, Integer> getScoreboard() {
       return game.getScoreboard();
    }

    /**
     * Emits a structured INFO log line for a successful game action.
     * Fields are pushed into MDC so JSON logging includes them under the
     * "mdc" object (Quarkus's default JSON log format), making them
     * extractable as Loki labels/structured metadata once shipped (see
     * k8s/31-alloy-config.yaml).
     */
    private void logAction(String action, Map<String, String> fields) {
        MDC.put("gameId", game.getId().uuid().toString());
        MDC.put("action", action);
        MDC.put("phase", game.getPhase().type().name());
        MDC.put("roundNumber", String.valueOf(game.getRoundCounter()));
        fields.forEach(MDC::put);
        try {
            LOG.infof("game action: %s", action);
        } finally {
            MDC.clear();
        }
    }

    private void logActionFailed(String action, RuntimeException e) {
        MDC.put("gameId", game.getId().uuid().toString());
        MDC.put("action", action);
        MDC.put("phase", game.getPhase().type().name());
        try {
            LOG.warnf("game action failed: %s - %s", action, e.getMessage());
        } finally {
            MDC.clear();
        }
    }

}
