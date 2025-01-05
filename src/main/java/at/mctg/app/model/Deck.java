package at.mctg.app.model;

import lombok.*;
import java.util.ArrayList;
import java.util.List;

//Represents a deck of up to 4 cards for a user.

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Deck {


    private String username;

    private List<Card> cards = new ArrayList<>();

    public boolean addCard(Card card) {
        if (this.cards.size() < 4) {
            this.cards.add(card);
            return true;
        }
        return false;
    }

    public void removeCard(Card card) {
        this.cards.remove(card);
    }
}
