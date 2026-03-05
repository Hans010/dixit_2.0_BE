package com.burnout.dixit.game.domain;

import com.burnout.dixit.game.command.*;
import com.burnout.dixit.game.domain.phase.*;
import jakarta.xml.bind.SchemaOutputResolver;

import java.util.Map;
import java.util.Set;

public final class GameTransition {

    private static final Map<Class<? extends GamePhase>, Set<Class<? extends GamePhase>>> ALLOWED = Map.of(
            Lobby.class, Set.of(StartRound.class),
            StartRound.class, Set.of(StorytellerChoice.class),
            StorytellerChoice.class, Set.of(CardSubmission.class),
            CardSubmission.class, Set.of(Voting.class),
            Voting.class, Set.of(Scoring.class),
            Scoring.class, Set.of(StartRound.class, GameOver.class)
    );

    public static boolean isAllowed(GamePhase from, GamePhase to) {
        return ALLOWED.getOrDefault(from.getClass(), Set.of()).contains(to.getClass());
    }

    private GameTransition() {}
}
