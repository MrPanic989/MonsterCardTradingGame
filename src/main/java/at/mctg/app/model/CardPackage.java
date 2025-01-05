package at.mctg.app.model;

import lombok.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

//Represents a card package (5 cards).

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CardPackage {

    private UUID packageId;

    private boolean purchased;

    //In code, I want to store the 5 cards that belong to this package.
    private List<Card> cards = new ArrayList<>();

    //Helper method: set the 5 cards for this package.
    public void setCards(List<Card> newCards) {
        if (newCards.size() == 5) {
            this.cards = newCards;
        } else {
            System.err.println("Ein Packet muss genau 5 Karten enthalten!");
        }
    }
}

