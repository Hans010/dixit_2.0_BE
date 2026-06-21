package com.burnout.dixit.game.domain;

import com.burnout.dixit.common.CardId;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The full set of Dixit cards, in draw order. Cards are removed from the
 * draw pile as they're drawn into player hands, but remain resolvable via
 * lookup() for the lifetime of the deck - hands and submissions only ever
 * store a CardId, so something needs to map that back to a filename.
 */
public class Deck {

    private final Deque<Card> drawPile;
    private final Map<CardId, Card> allCards;

    private Deck(List<Card> cards) {
        this.drawPile = new ArrayDeque<>(cards);
        this.allCards = new HashMap<>(cards.size());
        cards.forEach(card -> allCards.put(card.id(), card));
    }

    /**
     * Builds a deck from filenames in the form "card_<N>.png" (no zero
     * padding), shuffled into a fresh random order. Each card gets a freshly
     * generated CardId - card identity within a game is per-deck, not tied
     * to the filename itself, so the same image set can be reshuffled into
     * a new deck for every game.
     */
    public static Deck generate(int cardCount) {
        List<Card> cards = new ArrayList<>(cardCount);
        for (int i = 1; i <= cardCount; i++) {
            String filename = "card_" + i + ".png";
            cards.add(new Card(CardId.newId(), filename));
        }
        Collections.shuffle(cards);
        return new Deck(cards);
    }

    public Card drawCard() {
        Card card = drawPile.poll();
        if (card == null) {
            throw new IllegalStateException("Deck is empty - no cards left to draw");
        }
        return card;
    }

    public Card lookup(CardId cardId) {
        Card card = allCards.get(cardId);
        if (card == null) {
            throw new IllegalArgumentException("Unknown card: " + cardId.uuid());
        }
        return card;
    }

    public int remaining() {
        return drawPile.size();
    }

    public boolean isEmpty() {
        return drawPile.isEmpty();
    }
}
