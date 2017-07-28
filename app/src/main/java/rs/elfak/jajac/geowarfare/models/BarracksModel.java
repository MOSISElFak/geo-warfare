package rs.elfak.jajac.geowarfare.models;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class BarracksModel extends StructureModel {

    public Map<String, Integer> availableUnits = new HashMap<>();

    public BarracksModel() {
        // Default constructor required for calls to DataSnapshot.getValue(BarracksModel.class)
    }

    public BarracksModel(StructureType type, String ownerId) {
        super(type, ownerId);
        for (UnitType unitType : UnitType.values()) {
            this.availableUnits.put(unitType.toString(), 0);
        }
    }

    @Exclude
    private Map<UnitType, Integer> getAvailableForLevel(int level) {
        Map<UnitType, Integer> result = new HashMap<>();

        for (UnitType unitType : UnitType.values()) {
            int count = getCostForLevel(level) / unitType.getBaseCost();
            result.put(unitType, count);
        }

        return result;
    }

    @Exclude
    public Map<UnitType, Integer> getCurrentAvailable() {
        return getAvailableForLevel(this.level);
    }

    @Exclude
    public Map<UnitType, Integer> getNextAvailable() {
        return getAvailableForLevel(this.level + 1);
    }

    @Exclude
    public boolean canUpgrade() {
        return this.level < this.type.getMaxLevel();
    }

    @Exclude
    public int getUpgradeCost() {
        return getCostForLevel(this.level + 1);
    }

    @Exclude
    private int getCostForLevel(int level) {
        if (level == 1) {
            return this.type.getBaseCost();
        } else {
            return level * level * getCostForLevel(level - 1);
        }
    }

}
