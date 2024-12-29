package at.mctg.app.model;

import lombok.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a card package (5 cards).
 * In DB: packages(package_id, purchased).
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class CardPackage {

    /**
     * The primary key in the DB: "package_id" (UUID).
     */
    private UUID packageId;

    /**
     * Whether the package was already purchased (true/false).
     * In DB: packages.purchased
     */
    private boolean purchased;

    /**
     * In code, I want to store the 5 cards that belong to this package.
     * In DB: these 5 cards have "cards.package_id = this.packageId"
     */
    private List<Card> cards = new ArrayList<>();

    /**
     * Helper method: set the 5 cards for this package.
     */
    public void setCards(List<Card> newCards) {
        if (newCards.size() == 5) {
            this.cards = newCards;
        } else {
            System.err.println("Ein Packet muss genau 5 Karten enthalten!");
        }
    }
}

