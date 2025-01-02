package at.mctg.app.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.UUID;

//A reduced Card view: only { "Id", "Name", "Damage" }
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CardDTO {

    @JsonAlias({"Id","id"})
    @JsonProperty("Id")
    private UUID id;

    @JsonAlias({"Name","name"})
    @JsonProperty("Name")
    private String name;

    @JsonAlias({"Damage","damage"})
    @JsonProperty("Damage")
    private double damage;
}