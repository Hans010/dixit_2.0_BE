package com.burnout.dixit.game.service;

import com.burnout.dixit.game.domain.Card;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;

/**
 * Combines a card's filename with the configured base URL (see
 * dixit.cards.base-url in application.properties) to produce the URL the
 * frontend uses to render the card image. The backend never serves or
 * stores image bytes itself.
 */
@ApplicationScoped
public class CardImageResolver {

    @ConfigProperty(name = "dixit.cards.base-url")
    String baseUrl;

    public String resolve(Card card) {
        String base = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return base + "/" + card.filename();
    }
}
