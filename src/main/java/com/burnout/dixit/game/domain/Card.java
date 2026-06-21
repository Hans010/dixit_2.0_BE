package com.burnout.dixit.game.domain;

import com.burnout.dixit.common.CardId;

/**
 * A single Dixit card. The backend never stores image bytes — `filename`
 * is combined with a configured base URL (see CardImageResolver) to produce
 * the URL the frontend uses to actually render the image.
 */
public record Card(CardId id, String filename) {
}
