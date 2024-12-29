package at.mctg.app.model;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.UUID;

/**
 * Represents a single card entity.
 * Maps to the "cards" table in the DB.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Card {

    /**
     * The primary key in the DB: "card_id" (UUID).
     * In DB: cards.card_id
     */
    @JsonAlias({"Id", "id"})
    private UUID cardId;

    /**
     * The card's name, e.g. "Dragon", "WaterGoblin"...
     * In DB: cards.name
     */
    @JsonAlias({"Name", "name"})
    private String name;

    /**
     * The base damage of the card.
     * In DB: cards.damage
     */
    @JsonAlias({"Damage", "damage"})
    private double damage;

    /**
     * The element type, e.g. "FIRE", "WATER", "NORMAL".
     * In DB: cards.element_type
     */
    private String elementType;

    /**
     * The card type, e.g. "MONSTER" or "SPELL".
     * In DB: cards.card_type
     */
    @JsonAlias({"Type", "type"})
    private String cardType;

    /**
     * References the user (person.username) who currently owns this card.
     * In DB: cards.owner (VARCHAR(25))
     */
    private String ownerUsername;

    /**
     * References the package to which this card belongs (if any).
     * In DB: cards.package_id (UUID)
     */
    private UUID packageId;
}
