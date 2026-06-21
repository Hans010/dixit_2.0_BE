package com.burnout.dixit.game.persistence;

import com.burnout.dixit.common.CardId;
import com.burnout.dixit.common.GameId;
import com.burnout.dixit.common.PlayerId;
import com.burnout.dixit.game.domain.*;
import com.burnout.dixit.game.domain.phase.*;
import com.burnout.dixit.game.persistence.GameSnapshot.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Converts between the live Game domain object and GameSnapshot, the
 * plain-data DTO that actually gets serialized to Redis. See GameSnapshot
 * for why this indirection exists instead of serializing Game directly.
 */
public final class GameMapper {

    // Lobby only carries data before a game starts; once StartGame fires,
    // the phase moves on and these values are no longer read anywhere, so
    // a fixed default is fine to use both when capturing and restoring.
    private static final int LOBBY_MIN_PLAYERS = 3;
    private static final int LOBBY_MAX_PLAYERS = 5;

    private GameMapper() {
    }

    public static GameSnapshot toSnapshot(Game game) {
        GamePhase phase = game.getPhase();

        List<PlayerSnapshot> players = game.getPlayers().stream()
                .map(p -> new PlayerSnapshot(p.id().uuid(), p.name()))
                .toList();

        RoundSnapshot roundSnapshot = toRoundSnapshot(game.getCurrentRound());

        DeckSnapshot deckSnapshot = game.getDeck() == null ? null : new DeckSnapshot(
                game.getDeck().drawPileSnapshot().stream().map(GameMapper::toCardSnapshot).toList(),
                game.getDeck().allCardsSnapshot().stream().map(GameMapper::toCardSnapshot).toList()
        );

        Map<UUID, List<UUID>> hands = game.getHands().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey().uuid(),
                        e -> e.getValue().stream().map(CardId::uuid).toList()
                ));

        Integer lobbyMin = phase instanceof Lobby lobby ? lobby.minPlayers() : null;
        Integer lobbyMax = phase instanceof Lobby lobby ? lobby.maxPlayers() : null;

        return new GameSnapshot(
                game.getId().uuid(),
                phase.type().name(),
                lobbyMin,
                lobbyMax,
                players,
                game.getRoundCounter(),
                roundSnapshot,
                game.getRawScoreboard(),
                deckSnapshot,
                hands,
                game.getLastUpdated()
        );
    }

    public static Game fromSnapshot(GameSnapshot snapshot) {
        List<Player> players = snapshot.players().stream()
                .map(p -> new Player(new PlayerId(p.id()), p.name()))
                .collect(Collectors.toCollection(ArrayList::new));

        GamePhase phase = toPhase(snapshot.phaseType());

        Game game = new Game(new GameId(snapshot.gameId()), phase, players);

        Round round = fromRoundSnapshot(snapshot.currentRound(), players);

        Deck deck = snapshot.deck() == null ? null : Deck.restore(
                snapshot.deck().drawPile().stream().map(GameMapper::fromCardSnapshot).toList(),
                snapshot.deck().allCards().stream().map(GameMapper::fromCardSnapshot).toList()
        );

        Map<PlayerId, List<CardId>> hands = snapshot.hands().entrySet().stream()
                .collect(Collectors.toMap(
                        e -> new PlayerId(e.getKey()),
                        e -> e.getValue().stream().map(CardId::new).collect(Collectors.toCollection(ArrayList::new))
                ));

        // Game.scoreboard is null until the first round starts (see
        // Game.startNewRound()'s "if (scoreboard == null) initializeScoreboard()"
        // guard). If we're restoring a game still in Lobby, scoreboard must
        // stay null too, or that guard never fires and the scoreboard is
        // stuck empty forever once the game does start.
        Map<UUID, Integer> scoreboard = phase instanceof Lobby
                ? null
                : new HashMap<>(snapshot.scoreboard());

        game.restoreState(
                phase,
                snapshot.roundCounter(),
                round,
                scoreboard,
                deck,
                hands,
                snapshot.lastUpdated()
        );

        return game;
    }

    private static RoundSnapshot toRoundSnapshot(Round round) {
        if (round == null) {
            return null;
        }
        return new RoundSnapshot(
                round.getRoundNumber(),
                round.getStoryteller().id().uuid(),
                round.clue(),
                toUuidMap(round.getSubmissions()),
                toUuidMap(round.getVotes()),
                round.getRoundScore().entrySet().stream()
                        .collect(Collectors.toMap(e -> e.getKey().uuid(), Map.Entry::getValue)),
                round.noVotesInStorytellerCard
        );
    }

    private static Round fromRoundSnapshot(RoundSnapshot snapshot, List<Player> players) {
        if (snapshot == null) {
            return null;
        }
        Player storyteller = players.stream()
                .filter(p -> p.id().uuid().equals(snapshot.storytellerId()))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException(
                        "Storyteller " + snapshot.storytellerId() + " not found among restored players"));

        return Round.restore(
                snapshot.roundNumber(),
                storyteller,
                snapshot.clue(),
                fromUuidMap(snapshot.submissions()),
                fromUuidMap(snapshot.votes()),
                snapshot.roundScore().entrySet().stream()
                        .collect(Collectors.toMap(e -> new PlayerId(e.getKey()), Map.Entry::getValue)),
                snapshot.noVotesInStorytellerCard()
        );
    }

    private static Map<UUID, UUID> toUuidMap(Map<PlayerId, CardId> map) {
        return map.entrySet().stream()
                .collect(Collectors.toMap(e -> e.getKey().uuid(), e -> e.getValue().uuid()));
    }

    private static Map<PlayerId, CardId> fromUuidMap(Map<UUID, UUID> map) {
        return map.entrySet().stream()
                .collect(Collectors.toMap(e -> new PlayerId(e.getKey()), e -> new CardId(e.getValue())));
    }

    private static CardSnapshot toCardSnapshot(Card card) {
        return new CardSnapshot(card.id().uuid(), card.filename());
    }

    private static Card fromCardSnapshot(CardSnapshot snapshot) {
        return new Card(new CardId(snapshot.id()), snapshot.filename());
    }

    private static GamePhase toPhase(String phaseType) {
        return switch (PhaseType.valueOf(phaseType)) {
            case LOBBY -> new Lobby(LOBBY_MIN_PLAYERS, LOBBY_MAX_PLAYERS);
            case START_ROUND -> new StartRound();
            case STORYTELLER_CHOICE -> new StorytellerChoice();
            case CARD_SUBMISSION -> new CardSubmission();
            case VOTING -> new Voting();
            case SCORING -> new Scoring();
            case GAMEOVER -> new GameOver();
        };
    }
}
