package at.mctg.app.model;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.UUID;

//Represents a player/user in the system.
//Maps to the "person" table in the DB.

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class User {

    //The primary key in the DB: "id" (UUID).
        private UUID id;

    //The user's unique name in the system.
    @JsonAlias({"Username", "username"})
    private String username;

    @JsonAlias({"Password", "password"})
    private String password;

    //The authentication token, e.g. "kienboec-mtcgToken"
    private String authtoken;

    private boolean admin;

    @JsonAlias({"Name", "name"})
    private String name;
    @JsonAlias({"Bio", "bio"})
    private String bio;
    @JsonAlias({"Image", "image"})
    private String image;

    private int coins = 20;

    @JsonAlias({"Elo", "elo"})
    private int elo = 100;

    private int gamesPlayed;

    @JsonAlias({"Wins", "wins"})
    private int wins;
    @JsonAlias({"Loses", "loses"})
    private int losses;

}
