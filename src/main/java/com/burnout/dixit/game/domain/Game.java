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
            case ScoreRound cmd -> handleScoring();
            default -> throw new IllegalArgumentException("Unknown command: " + command);
        };
    }

    private void handleStartGame() {
        ensurePhase(Lobby.class);
        startNewRound();
    }

    private void startNewRound() {
        if (players.size() < 3) {
            throw new IllegalStateException("Cannot start game with fewer than 3 players");
        }

        roundCounter++;
        PlayerId storytellerId = setNewStoryteller().id();
        this.currentRound = new Round(roundCounter, storytellerId);
        this.phase = new StorytellerChoice();
        updateTimestamp();
    }

    private void handleChooseClue(ChooseClue cmd) {
        ensurePhase(StorytellerChoice.class);

        if (!cmd.getStorytellerId().equals(this.getCurrentRound().getStoryteller())) {
            throw new IllegalArgumentException("Only storyteller can choose clue: " + cmd.getStorytellerId());
        }
        StorytellerChoice phase = (StorytellerChoice) this.phase;

        phase.setClue(this.getCurrentRound(), cmd.getClue());
        phase.submitStorytellerCard(this.getCurrentRound(), cmd.getStorytellerCardId());

        transitionTo(new CardSubmission());
    }

    private void handleSubmitCard(SubmitCard cmd) {
        ensurePhase(CardSubmission.class);
        CardSubmission phase = (CardSubmission) this.phase;

        phase.submit(currentRound, cmd.getPlayerId(), cmd.getCardId());
        if (phase.allCardsSubmitted(currentRound, players.size())) {
            transitionTo(new Voting());
        }
    }

    private void handleVoteCard(VoteCard cmd) {
        ensurePhase(Voting.class);

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

        startNewRound();
        transitionTo(new StorytellerChoice());
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
}
