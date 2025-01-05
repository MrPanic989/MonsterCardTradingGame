package at.mctg.app.model;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.UUID;

//Represents a single card entity.
//Maps to the "cards" table in the DB.
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Card {

    //The primary key in the DB: "card_id" (UUID).
    @JsonAlias({"Id", "id"})
    private UUID cardId;

    //The card's name, e.g. "Dragon", "WaterGoblin"...
    @JsonAlias({"Name", "name"})
    private String name;

    @JsonAlias({"Damage", "damage"})
    private double damage;

    private String elementType;

    //The card type, e.g. "MONSTER" or "SPELL".
    @JsonAlias({"Type", "type"})
    private String cardType;

    //References the user (person.username) who currently owns this card.
    private String ownerUsername;

    //References the package to which this card belongs (if any).
    private UUID packageId;

    //For the Unique Feature.
    private int level;
}
