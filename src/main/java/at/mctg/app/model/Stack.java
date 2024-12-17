package at.mctg.app.model;

import java.util.ArrayList;
import java.util.List;

public class Stack {
    private List<Card> cards;

    public Stack() {
        this.cards = new ArrayList<>();
    }

    // Methoden zur Verwaltung des Stacks
    public void addCard(Card card) {
        this.cards.add(card);
    }

    public void removeCard(Card card) {
        this.cards.remove(card);
    }

    public List<Card> getCards() {
        return cards;
    }
}
