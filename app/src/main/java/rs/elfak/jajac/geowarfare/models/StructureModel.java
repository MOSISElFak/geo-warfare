package rs.elfak.jajac.geowarfare.models;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class StructureModel {

    public String id;
    public String ownerId;
    public StructureType type;
    public int level = 1;
    public Map<String, Integer> defenseUnits = new HashMap<>();

    public StructureModel() {
        // Default constructor required for calls to DataSnapshot.getValue(StructureModel.class)
    }

    public StructureModel(StructureType type, String ownerId) {
        this.type = type;
        this.ownerId = ownerId;

        for (UnitType unitType : UnitType.values()) {
            this.defenseUnits.put(unitType.toString(), 0);
        }
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
    protected int getCostForLevel(int level) {
        if (level == 1) {
            return this.type.getBaseCost();
        } else {
            return level * level * getCostForLevel(level - 1);
        }
    }

}
