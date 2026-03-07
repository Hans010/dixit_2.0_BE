package com.burnout.dixit.game.command;

import com.burnout.dixit.common.CardId;
import com.burnout.dixit.common.PlayerId;

public final class ChooseClue implements GameCommand {

    private final String clue;
    private final PlayerId storytellerId;
    private final CardId storytellerCardId;


    public ChooseClue(String clue, PlayerId storytellerId, CardId storytellerCardId)  {
        this.clue = clue;
        this.storytellerId = storytellerId;
        this.storytellerCardId = storytellerCardId;
    }

    public String getClue() {
        return clue;
    }

    public PlayerId getStorytellerId() {
        return storytellerId;
    }

    public CardId getStorytellerCardId() {
        return storytellerCardId;
    }
}
