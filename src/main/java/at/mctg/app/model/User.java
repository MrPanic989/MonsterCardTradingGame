package at.mctg.app.model;

import lombok.*;
import com.fasterxml.jackson.annotation.JsonAlias;

import java.util.UUID;

/**
 * Represents a player/user in the system.
 * Maps to the "person" table in the DB.
 */
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class User {

    /**
     * The primary key in the DB: "id" (UUID).
     * In DB: person.id
     */
    private UUID id;

    /**
     * The user's unique name in the system.
     * In DB: person.username
     */
    @JsonAlias({"Username", "username"})
    private String username;

    /**
     * The user's password.
     * In DB: person.password
     */
    @JsonAlias({"Password", "password"})
    private String password;

    /**
     * The authentication token, e.g. "kienboec-mtcgToken"
     * In DB: person.authtoken
     */
    private String authtoken;

    /**
     * Indicates if user is admin.
     * In DB: person.admin
     */
    private boolean admin;

    /**
     * Profile fields: name, bio, image
     * In DB: person.name, person.bio, person.image
     */
    @JsonAlias({"Name", "name"})
    private String name;
    @JsonAlias({"Bio", "bio"})
    private String bio;
    @JsonAlias({"Image", "image"})
    private String image;

    /**
     * The user's current coins (starts with 20).
     * In DB: person.coins
     */
    private int coins = 20;

    /**
     * The user's current ELO (starts with 100).
     * In DB: person.elo
     */
    @JsonAlias({"Elo", "elo"})
    private int elo = 100;

    /**
     * The total number of games played.
     * In DB: person.gamesplayed
     */
    private int gamesPlayed;

    /**
     * Number of wins and losses.
     * In DB: person.wins, person.losses
     */
    @JsonAlias({"Wins", "wins"})
    private int wins;
    @JsonAlias({"Loses", "loses"})
    private int losses;

}
