package rest;

import com.burnout.dixit.common.CardId;
import com.burnout.dixit.common.PlayerId;
import com.burnout.dixit.game.domain.Player;
import com.burnout.dixit.game.service.GameService;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.UUID;

@Path("/game")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class GameResource {

    @Inject
    GameService gameService;

    @GET
    @Path("/state")
    public GameStateResponse getState() {
        Player storyteller = gameService.getStoryteller();
        return new GameStateResponse(
                gameService.getCurrentPhase(),
                gameService.getRoundNumber(),
                storyteller != null ? storyteller.id().uuid() : null,
                storyteller != null ? storyteller.name() : null
        );
    }

    @POST
    @Path("/start")
    public Response startGame() {
        try {
            gameService.startGame();
            return Response.ok("Game started!").build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/clue")
    public Response chooseClue(ChooseClueRequest request) {
        try {
            gameService.chooseClue(
                new PlayerId(request.storytellerId()),
                request.clue(),
                new CardId(request.cardId())
            );
            return Response.ok("Clue chosen!").build();
        } catch (IllegalStateException | IllegalArgumentException e) {
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/submit")
    public Response submitCard(SubmitCardRequest request) {
        try {
            gameService.submitCard(
                new PlayerId(request.playerId()),
                new CardId(request.cardId())
            );
            return Response.ok("Card submitted!").build();
        } catch (IllegalStateException | IllegalArgumentException e) {
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/vote")
    public Response voteCard(VoteCardRequest request) {
        try {
            gameService.voteCard(
                new PlayerId(request.playerId()),
                new CardId(request.votedCardId())
            );
            return Response.ok("Vote submitted!").build();
        } catch (IllegalStateException | IllegalArgumentException e) {
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
        }
    }

    @POST
    @Path("/score")
    public Response scoreRound() {
        try {
            gameService.scoreRound();
            return Response.ok("Round scored!").build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
        }
    }

    @GET
    @Path("/scoreboard")
    public Response getScoreboard() {
        try {
            return Response.ok(gameService.getScoreboard()).build();
        } catch (IllegalStateException e) {
            return Response.status(Response.Status.CONFLICT).entity(e.getMessage()).build();
        }
    }

    // --- Request bodies ---

    public record ChooseClueRequest(UUID storytellerId, String clue, UUID cardId) {}
    public record SubmitCardRequest(UUID playerId, UUID cardId) {}
    public record VoteCardRequest(UUID playerId, UUID votedCardId) {}

    // --- Response bodies ---

    public record GameStateResponse(String phase, int roundNumber, UUID storytellerId, String storytellerName) {}

}
