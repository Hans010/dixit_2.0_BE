package rest;

import com.burnout.dixit.game.domain.Game;
import com.burnout.dixit.game.domain.Player;
import com.burnout.dixit.game.service.GameService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;

import java.util.List;

@Path("/player")
public class PlayerResource {

    @Inject
    GameService gameService;

    @GET
    @Path("/players")
    public String getPlayers() {
        Game game = gameService.getGame();
        String playerList = "Current Players: ";
        if (!game.getPlayers().isEmpty()) {
            for (Player player : game.getPlayers()) {
                playerList += "\n" + player.name();
            }
        } else playerList = "No Players";
        return playerList;
    }

    @POST
    @Path("/add/{name}")
    public String addPlayer(@PathParam("name") String name) {
        return gameService.addPlayer(name);
    }

    @DELETE
    @Path("/delete/{name}")
    @Produces("text/plain")
    public String deletePlayer(@PathParam("name") String name) {
        return gameService.removePlayer(name) ?
                "Player " + name + " removed." :
                "Player " + name + " not found.";
    }

}