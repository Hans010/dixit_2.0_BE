package com.burnout.dixit;

import com.burnout.dixit.common.PlayerId;
import com.burnout.dixit.game.domain.Game;
import com.burnout.dixit.game.domain.Player;
import com.burnout.dixit.game.domain.phase.GamePhase;
import com.burnout.dixit.game.domain.phase.Lobby;
import com.burnout.dixit.game.service.GameService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Path("/hello")
public class GreetingResource {

    @Inject
    GameService gameService;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello() {
        return "Hello from Quarkus REST";
    }

    @GET
    @Path("/{round}")
    @Produces(MediaType.TEXT_PLAIN)
    public String storyteller(int round) {
        return "current storyteller is " + gameService.getStoryteller(round);
    }
}

