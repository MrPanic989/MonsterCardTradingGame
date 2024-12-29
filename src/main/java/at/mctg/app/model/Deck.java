package at.mctg.app.model;

import lombok.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a deck of up to 4 cards for a user.
 * In the DB: deck table (username, card_id) as a mapping.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Deck {


    private String username;

    /**
     * The list of cards in this deck.
     * In DB, it's stored as multiple rows in `deck(username, card_id)`.
     */
    private List<Card> cards = new ArrayList<>();

    /**
     * Example method: add a Card, if fewer than 4.
     */
    public boolean addCard(Card card) {
        if (this.cards.size() < 4) {
            this.cards.add(card);
            return true;
        }
        return false;
    }

    /**
     * Remove a card from the deck.
     */
    public void removeCard(Card card) {
        this.cards.remove(card);
    }
}
