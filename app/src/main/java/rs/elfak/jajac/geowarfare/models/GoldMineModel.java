package rs.elfak.jajac.geowarfare.models;

import com.google.firebase.database.IgnoreExtraProperties;

import java.util.Map;

public class GoldMineModel extends StructureModel {
    
    public int gold = 0;

    public GoldMineModel() {
        // Default constructor required for calls to DataSnapshot.getValue(GoldMineModel.class)
    }

    public GoldMineModel(StructureType type, String ownerId) {
        super(type, ownerId);
    }

    private int getIncomeAtLevel(int level) {
        return getCostForLevel(level) / (level * 5);
    }

    public int getCurrentIncome() {
        return getIncomeAtLevel(this.level);
    }

    public int getNextIncome() {
        return getIncomeAtLevel(this.level + 1);
    }

    public boolean canUpgrade() {
        return this.level < this.type.getMaxLevel();
    }

    public int getUpgradeCost() {
        return getCostForLevel(this.level + 1);
    }

    private int getCostForLevel(int level) {
        if (level == 1) {
            return this.type.getBaseCost();
        } else {
            return level * level * getCostForLevel(level - 1);
        }
    }

}
