package at.mctg.app.model;


import java.util.ArrayList;
import java.util.List;

public class Deck {
    private List<Card> cards;

    public Deck() {
        this.cards = new ArrayList<>();
    }

    // Methoden zur Verwaltung des Decks
    public boolean addCard(Card card) {
        if (this.cards.size() < 4) {
            this.cards.add(card);
            return true;
        } else {
            // Deck ist voll
            return false;
        }
    }

    public void removeCard(Card card) {
        this.cards.remove(card);
    }

    public List<Card> getCards() {
        return cards;
    }
}
