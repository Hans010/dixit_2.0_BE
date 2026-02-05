package com.burnout.dixit.game.command;

import com.burnout.dixit.common.CardId;
import com.burnout.dixit.common.PlayerId;

public record SubmitCard(PlayerId player, CardId card) implements GameCommand {
}
