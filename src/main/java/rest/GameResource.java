package rest;

import com.burnout.dixit.game.service.GameService;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@Path("/game")
public class GameResource {

    @Inject
    GameService gameService;

    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hi(){
        return "hello! You're in the game API";
    }

    @GET
    @Path("/storyteller")
    @Produces(MediaType.TEXT_PLAIN)
    public String getStoryteller() {
        return gameService.getStoryteller();
    }

    @POST
    @Path("/roundStart")
    public String startRound(){
        return "Let's go!";
    }
}
