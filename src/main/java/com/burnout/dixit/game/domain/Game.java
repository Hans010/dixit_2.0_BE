package com.burnout.dixit.game.domain;

import com.burnout.dixit.common.GameId;
import com.burnout.dixit.common.PlayerId;
import com.burnout.dixit.game.command.*;
import com.burnout.dixit.game.domain.phase.*;

import java.time.Instant;
import java.util.List;

public class Game {

    private final GameId id;
    private GamePhase phase;
    private final List<Player> players;
    private Round currentRound;
    private int roundCounter;
    private Instant lastUpdated;

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

    public void handle(GameCommand command) {
        switch (command) {
            case StartGame cmd -> handleStartGame();
            case ChooseClue cmd -> handleChooseClue(cmd);
            case SubmitCard cmd -> handleSubmitCard(cmd);
            case VoteCard cmd -> handleVoteCard(cmd);
            case NextRound cmd -> handleNextRound();
            default -> throw new IllegalArgumentException("Unknown command: " + command);
        };
    }

    private void handleStartGame() {
        ensurePhase(Lobby.class);
        startNewRound();
    }

    private void handleChooseClue(ChooseClue cmd) {
        ensurePhase(StorytellerChoice.class);

        if (!cmd.storytellerId().equals(this.getCurrentRound().storyteller())) {
            throw new IllegalArgumentException("Only storyteller can choose clue: " + cmd.storytellerId());
        }
        currentRound.setClue(cmd.clue());

        transitionTo(new CardSubmission())

    }


    private void startNewRound() {
        if (players.size() < 3) {
            throw new IllegalStateException("Cannot start game with fewer than 3 players");
        }

        roundCounter++;

        PlayerId storytellerId = currentStoryteller().id();

        this.currentRound = new Round(roundCounter, storytellerId);

        this.phase = new StorytellerChoice(storytellerId);
        updateTimestamp();
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


    public Player currentStoryteller() {
        return players.get((roundCounter -1) % players.size());
    }
}
