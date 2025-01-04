package at.mctg.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class TradeDTO {

    @JsonAlias({"Id","id"})
    @JsonProperty("Id")
    private UUID id;

    @JsonAlias({"CardToTrade", "cardtotrade"})
    @JsonProperty("CardToTrade")
    private UUID cardToTradeId;

    @JsonAlias({"Type", "type"})
    @JsonProperty("Type")
    private String requiredCardType;

    @JsonAlias({"MinimumDamage", "minimumDamage"})
    @JsonProperty("MinimumDamage")
    private double minimumDamage;
}