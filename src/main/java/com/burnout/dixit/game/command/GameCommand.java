package com.burnout.dixit.game.command;

public sealed interface GameCommand permits StartGame, ChooseClue, SubmitCard, VoteCard, NextRound {
}
