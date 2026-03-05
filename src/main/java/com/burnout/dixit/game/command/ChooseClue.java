package com.burnout.dixit.game.command;

import com.burnout.dixit.common.PlayerId;

public record ChooseClue(String clue, PlayerId storytellerId) implements GameCommand {
}
