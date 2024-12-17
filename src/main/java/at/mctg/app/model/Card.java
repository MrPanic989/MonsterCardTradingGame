package at.mctg.app.model;

public class Card {
    private String id;
    private String name;
    private double damage;
    private ElementType elementType;
    private CardType cardType;

    public Card(String id, String name, double damage, ElementType elementType, CardType cardType) {
        this.id = id;
        this.name = name;
        this.damage = damage;
        this.elementType = elementType;
        this.cardType = cardType;
    }

    // Getter
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getDamage() {
        return damage;
    }

    public ElementType getElementType() {
        return elementType;
    }

    public CardType getCardType() {
        return cardType;
    }
}
