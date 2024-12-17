package at.mctg.app.model;

import java.util.ArrayList;
import java.util.List;

public class Package {
    private List<Card> cards;

    public Package() {
        this.cards = new ArrayList<>();
    }

    public Package(List<Card> cards) {
        if (cards.size() == 5) {
            this.cards = cards;
        } else {
            System.out.println("Ein Paket muss genau 5 Karten enthalten.");
        }
    }

    // Getter und Setter
    public List<Card> getCards() {
        return cards;
    }

    public void setCards(List<Card> cards) {
        if (cards.size() == 5) {
            this.cards = cards;
        } else {
            System.out.println("Ein Paket muss genau 5 Karten enthalten.");
        }
    }
}
