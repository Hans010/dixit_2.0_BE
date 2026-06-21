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

import java.util.ArrayList;
import java.util.Map;
import java.util.UUID;

@ApplicationScoped
public class GameService {

    GamePhase lobby = new Lobby(3,5);

    Game game = new Game(new GameId(UUID.randomUUID()), lobby,new ArrayList<Player>());

    public String addPlayer(String name) {
        game.addPlayer(name);
        return name + " has been added";
    }

    public Game getGame() {
        return game;
    }

    public boolean removePlayer(String name) {
        return game.removePlayer(name);
    }

    public void startGame() {
        game.handle(new StartGame());
    }

    public void chooseClue(PlayerId storytellerId, String clue, CardId cardId) {
        game.handle(new ChooseClue(clue, storytellerId, cardId));
    }

    public void submitCard(PlayerId playerId, CardId cardId) {
        game.handle(new SubmitCard(playerId, cardId));
    }

    public void voteCard(PlayerId playerId, CardId votedCardId) {
        game.handle(new VoteCard(playerId, votedCardId));
    }

    public void scoreRound() {
        game.handle(new ScoreRound());
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

}
