package com.burnout.dixit.game.command;

import com.burnout.dixit.common.CardId;
import com.burnout.dixit.common.PlayerId;

public record VoteCard(PlayerId playerId, CardId votedCardId) implements GameCommand {
}
