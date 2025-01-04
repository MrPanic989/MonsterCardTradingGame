package at.mctg.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonAlias;

//A reduced Stats View
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class StatsDTO {
    @JsonAlias({"Name","name"})
    @JsonProperty("Name")
    private String name;

    @JsonAlias({"ELO","elo", "Elo"})
    @JsonProperty("Elo")
    private int elo;

    @JsonAlias({"Wins","wins"})
    @JsonProperty("Wins")
    private int wins;

    @JsonAlias({"Losses","losses"})
    @JsonProperty("Losses")
    private int losses;
}
