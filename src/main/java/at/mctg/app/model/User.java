package at.mctg.app.model;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor

public class User {
    @JsonAlias({"Username", "username"})
    private String username;
    @JsonAlias({"Password", "password"})
    private String password;

    private int id;
    private String name;
    private String bio;
    private String image;
    private int coins;
    private int elo;
    private List<Card> stack;
    private List<Card> deck;
    //private Deck deck;

    //Benötigt für das Filtern der User
    public User(String username, String name, String bio, String image) {
        setUsername(username);
        setName(name);
        setBio(bio);
        setImage(image);
    }

    /*
    //Default-Konstruktor für Jackson
    public User() {}

    public User(String username, String password) {
        this.username = username;
        this.password = password;
        this.coins = 20; // Jeder Benutzer startet mit 20 Münzen
        this.elo = 100;  // Start-ELO
        this.stack = new ArrayList<>();
        this.deck = new ArrayList<>();
    }
    // Getter und Setter
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getCoins() {
        return coins;
    }


    public int getElo() {
        return elo;
    }

    public void setElo(int elo) {
        this.elo = elo;
    }

    public List<Card> getStack() {
        return stack;
    }

    public List<Card> getDeck() {
        return deck;
    }

    // Methoden zur Verwaltung der Coins
    public void reduceCoins(int amount) {
        this.coins -= amount;
    }

    public void addCoins(int amount) {
        this.coins += amount;
    }
    */
    // Methoden zur Verwaltung des Stacks
    public void addCardToStack(Card card) {
        this.stack.add(card);
    }

    public void removeCardFromStack(Card card) {
        this.stack.remove(card);
    }

    // Methoden zur Verwaltung des Decks
    public boolean addCardToDeck(Card card) {
        if (this.deck.size() < 4) {
            this.deck.add(card);
            return true;
        } else {
            // Deck ist voll
            return false;
        }
    }

    public void removeCardFromDeck(Card card) {
        this.deck.remove(card);
    }

}