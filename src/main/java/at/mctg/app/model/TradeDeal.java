package at.mctg.app.model;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonAlias;
import java.util.UUID;

//Represents a single trading deal.

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class TradeDeal {

    @JsonAlias({"Id", "id"})
    private UUID id;

    @JsonAlias({"CardToTrade", "cardtotrade"})
    private UUID cardToTradeId;

    @JsonAlias({"Type", "type"})
    private String requiredCardType;

    @JsonAlias({"MinimumDamage", "minimumDamage"})
    private double minimumDamage;

    private String username;
}