package com.burnout.dixit.game.domain;

import com.burnout.dixit.common.CardId;
import com.burnout.dixit.common.GameId;
import com.burnout.dixit.common.PlayerId;
import com.burnout.dixit.game.command.*;
import com.burnout.dixit.game.domain.phase.*;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class Game {

    private static final int WINNING_SCORE = 30;
    private static final int HAND_SIZE = 6;
    private static final int DECK_SIZE = 198;

    private final GameId id;
    private GamePhase phase;
    private final List<Player> players;
    private Round currentRound;
    private int roundCounter;
    private Instant lastUpdated;
    private Map<UUID, Integer> scoreboard = null;
    private Deck deck;
    private final Map<PlayerId, List<CardId>> hands = new HashMap<>();

    public Game(GameId id, GamePhase initialPhase, List<Player> players) {
        this.id = id;
        this.phase = initialPhase;
        this.players = players;
        this.roundCounter = 0;
        this.lastUpdated = Instant.now();
    }

    public Instant getLastUpdated() {
        return lastUpdated;
    }

    public int getRoundCounter() {
        return roundCounter;
    }

    public Round getCurrentRound() {
        return currentRound;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public GamePhase getPhase() {
        return phase;
    }

    public GameId getId() {
        return id;
    }

    public void updateTimestamp() {
        this.lastUpdated = Instant.now();
    }


    public void addPlayer(String name) {
        Player player = new Player(PlayerId.newId(), name);
        this.players.add(player);
    }

    public boolean removePlayer(String name) {
        return this.players.removeIf(player -> player.name().equals(name));
    }

    public void handle(GameCommand command) {
        switch (command) {
            case StartGame cmd -> handleStartGame();
            case ChooseClue cmd -> handleChooseClue(cmd);
            case SubmitCard cmd -> handleSubmitCard(cmd);
            case VoteCard cmd -> handleVoteCard(cmd);
            case ScoreRound cmd -> handleScoring();
            default -> throw new IllegalArgumentException("Unknown command: " + command);
        };
    }

    private void handleStartGame() {
        ensurePhase(Lobby.class);
        deck = Deck.generate(DECK_SIZE);
        dealHands();
        startNewRound();
    }

    private void dealHands() {
        for (Player player : players) {
            List<CardId> hand = new ArrayList<>(HAND_SIZE);
            for (int i = 0; i < HAND_SIZE; i++) {
                hand.add(deck.drawCard().id());
            }
            hands.put(player.id(), hand);
        }
    }

    private void startNewRound() {
        if (players.size() < 3) {
            throw new IllegalStateException("Cannot start game with fewer than 3 players");
        }
        if (scoreboard == null) {
            initializeScoreboard();
        }

        roundCounter++;
        Player storyteller = setNewStoryteller();
        this.currentRound = new Round(roundCounter, storyteller);
        this.phase = new StorytellerChoice();
        updateTimestamp();
    }

    private void handleChooseClue(ChooseClue cmd) {
        ensurePhase(StorytellerChoice.class);

        if (!cmd.getStorytellerId().equals(this.getCurrentRound().getStoryteller().id())) {
            throw new IllegalArgumentException("Only storyteller can choose clue: " + cmd.getStorytellerId());
        }
        playCardFromHand(cmd.getStorytellerId(), cmd.getStorytellerCardId());

        StorytellerChoice phase = (StorytellerChoice) this.phase;

        phase.setClue(this.getCurrentRound(), cmd.getClue());
        phase.submitStorytellerCard(this.getCurrentRound(), cmd.getStorytellerCardId());

        transitionTo(new CardSubmission());
    }

    private void handleSubmitCard(SubmitCard cmd) {
        ensurePhase(CardSubmission.class);
        ensurePlayerExists(cmd.getPlayerId());
        playCardFromHand(cmd.getPlayerId(), cmd.getCardId());

        CardSubmission phase = (CardSubmission) this.phase;

        phase.submit(currentRound, cmd.getPlayerId(), cmd.getCardId());
        if (phase.allCardsSubmitted(currentRound, players.size())) {
            transitionTo(new Voting());
        }
    }

    private void handleVoteCard(VoteCard cmd) {
        ensurePhase(Voting.class);
        ensurePlayerExists(cmd.playerId());

        Voting phase = (Voting) this.phase;
        phase.vote(currentRound, cmd.playerId(), cmd.votedCardId());

        if (phase.allVotesSubmitted(currentRound, players.size())) {
            transitionTo(new Scoring());
        }
    }

    private void handleScoring() {
        ensurePhase(Scoring.class);

        Scoring phase = (Scoring) this.phase;
        phase.scoreRound(currentRound);
        updateScores();

        if (hasWinner()) {
            transitionTo(new GameOver());
        } else {
            replenishHands();
            startNewRound();
        }
    }

    /**
     * Every player played exactly one card this round (storyteller's clue
     * card plus each other player's submission), so every hand is down to
     * HAND_SIZE - 1. Draw one replacement per player to bring hands back to
     * full before the next round begins.
     */
    private void replenishHands() {
        for (Player player : players) {
            hands.get(player.id()).add(deck.drawCard().id());
        }
    }

    private boolean hasWinner() {
        return scoreboard.values().stream().anyMatch(score -> score >= WINNING_SCORE);
    }

    /**
     * Removes a card from a player's hand when it's played (as a clue card
     * or a submission), validating that the player actually holds it.
     */
    private void playCardFromHand(PlayerId playerId, CardId cardId) {
        List<CardId> hand = hands.get(playerId);
        if (hand == null || !hand.remove(cardId)) {
            throw new IllegalArgumentException("Player does not hold card: " + cardId.uuid());
        }
    }

    public List<CardId> getHand(PlayerId playerId) {
        return hands.getOrDefault(playerId, List.of());
    }

    public List<Card> getHandCards(PlayerId playerId) {
        return getHand(playerId).stream().map(deck::lookup).toList();
    }

    private void ensurePlayerExists(PlayerId playerId) {
        boolean exists = players.stream().anyMatch(p -> p.id().equals(playerId));
        if (!exists) {
            throw new IllegalArgumentException("Player not found: " + playerId.uuid());
        }
    }

    private void ensurePhase(Class<? extends GamePhase> expected) {
        if (!expected.isInstance(this.phase)) {
            throw new IllegalStateException("Game phase " + this.phase.getClass().getSimpleName() + " is not " + expected.getSimpleName());
        }
    }

    private void transitionTo(GamePhase newPhase) {
        if (!GameTransition.isAllowed(this.phase, newPhase)) {
            throw new IllegalStateException( "Illegal transition from " +
                    phase.getClass().getSimpleName() +
                    " to " +
                    newPhase.getClass().getSimpleName());
        }
        this.phase = newPhase;
        updateTimestamp();
    }


    public Player setNewStoryteller() {
        return players.get((roundCounter -1) % players.size());
    }

    private void initializeScoreboard() {
        scoreboard = new HashMap<>();
        players.forEach(player -> scoreboard.put(player.id().uuid(), 0));
    }

    private void updateScores() {
        currentRound.getRoundScore().forEach((playerId, score) -> scoreboard.put(playerId.uuid(), scoreboard.get(playerId.uuid()) + score));
    }

    public Map<Player, Integer> getScoreboard() {
        Map<Player, Integer> scoreboard = new HashMap<>();
        players.forEach(player -> scoreboard.put(player, this.scoreboard.get(player.id().uuid())));
        return scoreboard;
    }

    /**
     * Raw scoreboard keyed by player UUID, as used internally and by
     * GameMapper for persistence. Returns an empty map if the game hasn't
     * started yet (scoreboard is only initialized on first round start).
     */
    public Map<UUID, Integer> getRawScoreboard() {
        return scoreboard == null ? Map.of() : scoreboard;
    }

    public Deck getDeck() {
        return deck;
    }

    public Map<PlayerId, List<CardId>> getHands() {
        return hands;
    }

    /**
     * Restores internal state from a previously persisted snapshot (see
     * GameMapper). Used only when rehydrating a Game from Redis - normal
     * gameplay never calls this.
     */
    public void restoreState(GamePhase phase, int roundCounter, Round currentRound,
                              Map<UUID, Integer> scoreboard, Deck deck,
                              Map<PlayerId, List<CardId>> hands, Instant lastUpdated) {
        this.phase = phase;
        this.roundCounter = roundCounter;
        this.currentRound = currentRound;
        this.scoreboard = scoreboard;
        this.deck = deck;
        this.hands.clear();
        this.hands.putAll(hands);
        this.lastUpdated = lastUpdated;
    }
}
