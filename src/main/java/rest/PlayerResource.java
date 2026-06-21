package rest;

import com.burnout.dixit.game.domain.Player;
import com.burnout.dixit.game.service.GameService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.List;
import java.util.UUID;

@Path("/player")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class PlayerResource {

    @Inject
    GameService gameService;

    @GET
    @Path("/players")
    public List<PlayerResponse> getPlayers() {
        return gameService.getGame().getPlayers().stream()
                .map(p -> new PlayerResponse(p.id().uuid(), p.name()))
                .toList();
    }

    @POST
    @Path("/add/{name}")
    public Response addPlayer(@PathParam("name") String name) {
        String result = gameService.addPlayer(name);
        return Response.ok(result).build();
    }

    @DELETE
    @Path("/delete/{name}")
    public Response deletePlayer(@PathParam("name") String name) {
        boolean removed = gameService.removePlayer(name);
        if (removed) {
            return Response.ok("Player " + name + " removed.").build();
        } else {
            return Response.status(Response.Status.NOT_FOUND).entity("Player " + name + " not found.").build();
        }
    }

    // --- Response body ---

    public record PlayerResponse(UUID id, String name) {}

}