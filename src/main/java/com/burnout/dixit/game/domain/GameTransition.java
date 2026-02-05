package com.burnout.dixit.game.domain;

import com.burnout.dixit.game.command.*;
import com.burnout.dixit.game.domain.phase.*;
import jakarta.xml.bind.SchemaOutputResolver;

public final class GameTransition {
    private GameTransition() {}

    public static Game apply(Game game, GameCommand command) {
        return switch (game.phase()) {
            case Lobby lobby -> handleLobby(game, lobby, command);
            case StorytellerChoice sc -> handleStorytellerChoice(game, sc, command);
            case CardSubmission cs -> handleCardSubmission(game, cs, command);
            case Voting voting ->  handleVoting(game, voting, command);
            case Scoring scoring ->  handleScoring(game, scoring, command);
        };
    }

    private static Game handleLobby (Game game, Lobby lobby, GameCommand command) {
        if (command instanceof StartGame startGame) {

            return game.initialGame(lobby.startGame(game.currentStoryteller().id()));
        }
        throw invalid(command, lobby);
    }

    private static Game handleStorytellerChoice(Game game, StorytellerChoice choice, GameCommand command) {
        if (command instanceof ChooseClue(String clue)) {
            return game.withPhase(choice.chooseClue(clue));
        }
        throw invalid(command, choice);
    }

    private static Game handleCardSubmission(Game game, CardSubmission cardSubmission, GameCommand command) {
        if(command instanceof SubmitCard submitCard) {
            return game.withPhase(cardSubmission.submit(
                    submitCard.player(),
                    submitCard.card(),
                    game.players().size()
            ));
        }
        throw invalid(command, cardSubmission);
    }

    private static Game handleVoting(Game game, Voting voting, GameCommand command) {
        if(command instanceof VoteCard voteCard) {
            return game.withPhase(voting.vote(voteCard.voter(), voteCard.votedPLayer(), game.players().size()));
        }
        throw invalid(command, voting);
    }

    private static Game handleScoring(Game game, Scoring scoring, GameCommand command) {
        if(command instanceof ChooseClue) {
            System.out.println("Choose Clue");
        }
        throw invalid(command, scoring);
    }

    private static IllegalStateException invalid(GameCommand command, GamePhase phase) {
        return new IllegalStateException(
                "Command " + command + " not allowed in phase " + phase.type()
        );
    }
}
