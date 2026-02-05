package com.burnout.dixit.game.command;

import com.burnout.dixit.common.PlayerId;

public record VoteCard(PlayerId voter, PlayerId votedPLayer) implements GameCommand {
}
