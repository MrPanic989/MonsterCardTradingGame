package at.mctg.app.service.battle;

import at.mctg.app.model.Card;
import at.mctg.app.model.User;

import java.util.Map;
import java.util.UUID;

//Separated "pure" battle logic class (damage rules, ELO changes and usage-based level-up)
//so thath it can be used fpr UnitTesting
public class BattleLogic {
    public BattleLogic() {}

    public void applyElo(User challenger, User opponent, boolean challengerWon) {
        if (challengerWon) {
            challenger.setElo(challenger.getElo() + 3);
            opponent.setElo(opponent.getElo() - 5);
            challenger.setWins(challenger.getWins() + 1);
            opponent.setLosses(opponent.getLosses() + 1);
        } else {
            // opponent won
            opponent.setElo(opponent.getElo() + 3);
            challenger.setElo(challenger.getElo() - 5);
            opponent.setWins(opponent.getWins() + 1);
            challenger.setLosses(challenger.getLosses() + 1);
        }
    }

    public double computeDamage(Card attacker, Card defender) {
        // Check special combos that set damage to 0 or cause instant defeat (from the specs)

        String attackersName = attacker.getName().toLowerCase();
        String defendersName = defender.getName().toLowerCase();
        final double youCannotTouchMe = 0.0;

        // "Goblins are too afraid of Dragons to attack " => Goblin does 0 damage
        if (attackersName.contains("goblin") && defendersName.contains("dragon")) {
            return youCannotTouchMe;
        }

        // "Wizzard can control Orks so they are not able to damage them" => Ork does 0 damage
        if (attackersName.contains("ork") && defendersName.contains("wizzard")) {
            return youCannotTouchMe;
        }

        // "The armor of Knights is so heavy that WaterSpells make them drown instantly."
        // => Knight instantly loses
        if (attackersName.contains("knight") && defendersName.contains("waterspell")) {
            return youCannotTouchMe; // "drown instantly"
        }

        // "The Kraken is immune against spells" => if attacker is Spell vs. defender is "kraken" => 0
        if (attacker.getCardType() != null && attacker.getCardType().toLowerCase().contains("spell")
                && defendersName.contains("kraken")) {
            return youCannotTouchMe;
        }

        // "The FireElves know Dragons since they were little and can evade their attacks."
        if (attackersName.contains("dragon") && defendersName.contains("fireelf")) {
            return youCannotTouchMe;
        }


        double baseDamage = attacker.getDamage();

        // It it is a pure monster fight, no element effect is added, so the
        // base Damage of the attacking Card is returned
        boolean attackerIsSpell = (attacker.getCardType() != null
                && attacker.getCardType().toLowerCase().contains("spell"));
        boolean defenderIsSpell = (defender.getCardType() != null
                && defender.getCardType().toLowerCase().contains("spell"));

        if (!attackerIsSpell && !defenderIsSpell) {
            return baseDamage;
        }

        // If at least one Card is spell, the following  advantage logic is added:
        // water -> fire => x2
        // fire -> normal => x2
        // normal -> water => x2
        // if not effective => x0.5
        // else normal => same

        String attackingElement =
                (attacker.getElementType() != null)
                        ? attacker.getElementType().toLowerCase() : "";

        String defindingElement =
                (defender.getElementType() != null)
                        ? defender.getElementType().toLowerCase() : "";

        // water -> fire => attacker *2
        if (attackingElement.equals("water") && defindingElement.equals("fire")) {
            return baseDamage * 2;
        }

        // fire -> normal => attacker *2
        if (attackingElement.equals("fire") && defindingElement.equals("normal")) {
            return baseDamage * 2;
        }

        // normal -> water => attacker *2
        if (attackingElement.equals("normal") && defindingElement.equals("water")) {
            return baseDamage * 2;
        }

        // not effective => half damage => (the inverse pairs)
        // fire -> water => attacker *0.5
        if (attackingElement.equals("fire") && defindingElement.equals("water")) {
            return baseDamage * 0.5;
        }

        // normal -> fire => attacker *0.5
        if (attackingElement.equals("normal") && defindingElement.equals("fire")) {
            return baseDamage * 0.5;
        }

        // water -> normal => attacker *0.5
        if (attackingElement.equals("water") && defindingElement.equals("normal")) {
            return baseDamage * 0.5;
        }

        // else no effect => baseDamage
        return baseDamage;
    }

    //This is a helper Method for the Unique Feature
    // potential card level-up and increase in strength
    //and returns if the card has leveled-up in this round
    public boolean incrementUsageAndMaybeLevelUp(Card card, Map<UUID, Integer> usageMap) {
        UUID cardId = card.getCardId();
        int oldCount = usageMap.getOrDefault(cardId, 0);
        int newCount = oldCount + 1;
        usageMap.put(cardId, newCount);

        // check if newCount is multiple of 5
        if (newCount % 5 == 0) {
            double oldDamage = card.getDamage();
            int oldLevel = card.getLevel();

            double newDamage = oldDamage * 1.2;
            int newLevel = oldLevel + 1;

            card.setDamage(newDamage);
            card.setLevel(newLevel);

            return true;
        }
        return false;
    }
}
