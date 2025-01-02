package at.mctg.app.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

//A DTO that only exposes Name, Bio, and Image of a user.

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class UserDTO {
    @JsonAlias({"Name", "name"})
    @JsonProperty("Name")
    private String name;

    @JsonAlias({"Bio", "bio"})
    @JsonProperty("Bio")
    private String bio;

    @JsonAlias({"Image", "image"})
    @JsonProperty("Image")
    private String image;
}
