package at.mctg.app.model;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.UUID;

/**
 * Represents a single trading deal.
 * In DB: trades(trade_id, card_id, required_type, required_element, required_damage, username).
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TradeDeal {

    /**
     * The unique ID of the trade (UUID).
     */
    @JsonAlias({"Id", "id"})
    private UUID id;

    /**
     * The card being offered for trade (the "card_id" from DB).
     */
    @JsonAlias({"CardToTrade", "cardtotrade"})
    private UUID cardToTradeId;

    /**
     * The card type required (monster or spell).
     */
    @JsonAlias({"Type", "type"})
    private String requiredCardType;

    private String requiredElement;

    /**
     * The minimum damage required from the offered card.
     */
    @JsonAlias({"MinimumDamage", "minimumDamage"})
    private double minimumDamage;


    /**
     * The user who created this trade.
     * In DB: trades.username
     */
    private String username;
}