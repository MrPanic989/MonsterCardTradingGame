package at.mctg.app.model;

public class TradeDeal {
    private String id;
    private String cardToTradeId;
    private String requiredCardType;
    private double minimumDamage;

    public TradeDeal(String id, String cardToTradeId, String requiredCardType, double minimumDamage) {
        this.id = id;
        this.cardToTradeId = cardToTradeId;
        this.requiredCardType = requiredCardType;
        this.minimumDamage = minimumDamage;
    }

    // Getter
    public String getId() {
        return id;
    }

    public String getCardToTradeId() {
        return cardToTradeId;
    }

    public String getRequiredCardType() {
        return requiredCardType;
    }

    public double getMinimumDamage() {
        return minimumDamage;
    }
}
