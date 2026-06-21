package com.burnout.dixit.game.service;

import com.burnout.dixit.common.CardId;
import com.burnout.dixit.common.GameId;
import com.burnout.dixit.common.PlayerId;
import com.burnout.dixit.game.command.*;
import com.burnout.dixit.game.domain.Card;
import com.burnout.dixit.game.domain.Game;
import com.burnout.dixit.game.domain.Player;
import com.burnout.dixit.game.domain.Round;
import com.burnout.dixit.game.domain.phase.GamePhase;
import com.burnout.dixit.game.domain.phase.Lobby;
import com.burnout.dixit.game.ws.GameSocket;
import com.burnout.dixit.game.ws.GameStateBroadcast;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.jboss.logging.Logger;
import org.jboss.logging.MDC;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class GameService {

    private static final Logger LOG = Logger.getLogger(GameService.class);

    @Inject
    GameRepository gameRepository;

    @Inject
    GameSocket gameSocket;

    @Inject
    ObjectMapper objectMapper;

    @Inject
    CardImageResolver cardImageResolver;

    private Game game;

    /**
     * On startup, restore the most recently known game from Redis if one
     * exists; otherwise start fresh. This means a backend restart doesn't
     * lose an in-progress game's state.
     *
     * Note: this still models a single active game, same as before - it
     * just survives restarts now. Supporting multiple concurrent games is
     * a separate change (would mean keying everything by gameId instead of
     * holding one Game field), not something this persistence layer alone
     * adds.
     */
    @PostConstruct
    void init() {
        game = gameRepository.findById(LAST_GAME_ID)
                .orElseGet(GameService::newGame);
    }

    // A fixed, well-known ID so the single in-memory game has a stable
    // Redis key to be restored from. Once multi-game support exists, this
    // goes away in favor of real per-game IDs end to end.
    private static final GameId LAST_GAME_ID = new GameId(UUID.fromString("00000000-0000-0000-0000-000000000001"));

    private static Game newGame() {
        GamePhase lobby = new Lobby(3, 5);
        return new Game(LAST_GAME_ID, lobby, new ArrayList<Player>());
    }

    public String addPlayer(String name) {
        game.addPlayer(name);
        persist(false);
        logAction("PLAYER_ADDED", Map.of("playerName", name));
        return name + " has been added";
    }

    public Game getGame() {
        return game;
    }

    public boolean removePlayer(String name) {
        boolean removed = game.removePlayer(name);
        if (removed) {
            persist(false);
        }
        logAction(removed ? "PLAYER_REMOVED" : "PLAYER_REMOVE_FAILED", Map.of("playerName", name));
        return removed;
    }

    public void startGame() {
        try {
            game.handle(new StartGame());
            persist(false);
            logAction("GAME_STARTED", Map.of());
        } catch (RuntimeException e) {
            logActionFailed("GAME_START_FAILED", e);
            throw e;
        }
    }

    public void chooseClue(PlayerId storytellerId, String clue, CardId cardId) {
        try {
            game.handle(new ChooseClue(clue, storytellerId, cardId));
            persist(false);
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
            persist(false);
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
            persist(false);
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
            // true: this broadcast (and only this one) should reveal the
            // storyteller's card for the round that was just scored. See
            // Game.handleScoring()/getLastRevealedStorytellerCardId() for
            // why this can't just be derived from current phase/round.
            persist(true);
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

    public List<Card> getHand(PlayerId playerId) {
        return game.getHandCards(playerId);
    }

    private void persist(boolean revealStorytellerCard) {
        gameRepository.save(game);
        broadcastState(revealStorytellerCard);
    }

    /**
     * Builds the current state payload and pushes it to every connected
     * WebSocket client. Serialization failures are logged but swallowed -
     * a broadcast problem shouldn't fail the underlying game command, since
     * the command itself already succeeded and was already persisted by
     * the time this runs.
     */
    private void broadcastState(boolean revealStorytellerCard) {
        try {
            GameStateBroadcast payload = buildBroadcastPayload(revealStorytellerCard);
            String json = objectMapper.writeValueAsString(payload);
            gameSocket.broadcast(json);
        } catch (Exception e) {
            LOG.warnf("Failed to broadcast game state: %s", e.getMessage());
        }
    }

    private GameStateBroadcast buildBroadcastPayload(boolean revealStorytellerCard) {
        Player storyteller = getStoryteller();
        Round currentRound = game.getCurrentRound();
        Map<String, Integer> scoreboard = new LinkedHashMap<>();
        getScoreboard().forEach((player, score) -> scoreboard.put(player.name(), score));

        int totalPlayers = game.getPlayers().size();

        GameStateBroadcast.SubmissionProgress submissions = currentRound != null
                ? new GameStateBroadcast.SubmissionProgress(currentRound.getSubmissions().size(), totalPlayers)
                : null;

        // Storyteller doesn't vote, so expected votes is total players minus one.
        GameStateBroadcast.VoteProgress votes = currentRound != null
                ? new GameStateBroadcast.VoteProgress(currentRound.getVotes().size(), Math.max(totalPlayers - 1, 0))
                : null;

        GameStateBroadcast.CardView storytellerCard = revealStorytellerCard
                ? resolveStorytellerCard()
                : null;

        return new GameStateBroadcast(
                getCurrentPhase(),
                getRoundNumber(),
                storyteller != null ? storyteller.id().uuid() : null,
                storyteller != null ? storyteller.name() : null,
                currentRound != null ? currentRound.clue() : null,
                submissions,
                votes,
                storytellerCard,
                scoreboard
        );
    }

    /**
     * Resolves the storyteller's card for the round that was just scored.
     * Only ever called with revealStorytellerCard=true, i.e. exactly once,
     * immediately after a ScoreRound command - never for any other
     * broadcast. This is what keeps the reveal scoped to that single
     * message instead of leaking into broadcasts for later rounds: the
     * underlying Game field (getLastRevealedStorytellerCardId()) isn't
     * cleared after a round ends, so without this scoping, every
     * subsequent broadcast would also show it - revealing the previous
     * round's answer before the current round's voting has even closed.
     */
    private GameStateBroadcast.CardView resolveStorytellerCard() {
        CardId storytellerCardId = game.getLastRevealedStorytellerCardId();
        if (storytellerCardId == null) {
            return null;
        }
        Card card = game.getDeck().lookup(storytellerCardId);
        return new GameStateBroadcast.CardView(card.id().uuid(), cardImageResolver.resolve(card));
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
