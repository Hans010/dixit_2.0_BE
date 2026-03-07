package com.burnout.dixit.game.command;

import com.burnout.dixit.common.CardId;
import com.burnout.dixit.common.PlayerId;

public final class SubmitCard implements GameCommand {
    private final PlayerId playerId;
    private final CardId cardId;

    public SubmitCard(PlayerId playerId, CardId cardId) {
        this.playerId = playerId;
        this.cardId = cardId;
    }

    public PlayerId getPlayerId() {
        return playerId;
    }

    public CardId getCardId() {
        return cardId;
    }
}
