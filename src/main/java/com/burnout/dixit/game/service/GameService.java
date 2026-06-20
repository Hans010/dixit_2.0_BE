package com.burnout.dixit.game.service;

import com.burnout.dixit.common.GameId;
import com.burnout.dixit.common.PlayerId;
import com.burnout.dixit.game.command.ChooseClue;
import com.burnout.dixit.game.domain.Game;
import com.burnout.dixit.game.domain.Player;
import com.burnout.dixit.game.domain.phase.GamePhase;
import com.burnout.dixit.game.domain.phase.Lobby;
import jakarta.enterprise.context.ApplicationScoped;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
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



}
