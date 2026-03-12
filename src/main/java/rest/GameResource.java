package rest;

import com.burnout.dixit.game.service.GameService;
import jakarta.inject.Inject;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;

@Path("/game")
public class GameResource {

    @Inject
    GameService gameService;

    @POST
    @Path("/roundStart")
    public String startRound(){
        return "Let's go!";
    }


}
