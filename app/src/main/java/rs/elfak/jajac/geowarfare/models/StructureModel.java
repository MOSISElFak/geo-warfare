package rs.elfak.jajac.geowarfare.models;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class StructureModel {

    protected String id;
    protected String ownerId;
    protected StructureType type;
    protected int level = 1;
    protected Map<String, Integer> defenseUnits = new HashMap<>();

    public StructureModel() {
        // Default constructor required for calls to DataSnapshot.getValue(StructureModel.class)
    }

    public StructureModel(StructureType type, String ownerId) {
        this.type = type;
        this.ownerId = ownerId;
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

    @Exclude
    public int getDefenseUnitCount(UnitType type) {
        if (this.defenseUnits.containsKey(type.toString())) {
            return this.defenseUnits.get(type.toString());
        } else {
            return 0;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(String ownerId) {
        this.ownerId = ownerId;
    }

    public StructureType getType() {
        return type;
    }

    public void setType(StructureType type) {
        this.type = type;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Map<String, Integer> getDefenseUnits() {
        Map<String, Integer> allUnits = new HashMap<>();

        for (UnitType unitType : UnitType.values()) {
            String typeName = unitType.toString();
            if (this.defenseUnits.containsKey(typeName)) {
                allUnits.put(typeName, this.defenseUnits.get(unitType.toString()));
            } else {
                allUnits.put(typeName, 0);
            }
        }
        return allUnits;
    }

    public void setDefenseUnits(Map<String, Integer> defenseUnits) {
        for (UnitType unitType : UnitType.values()) {
            String typeName = unitType.toString();
            if (!defenseUnits.containsKey(typeName) || defenseUnits.get(typeName) == 0) {
                this.defenseUnits.remove(typeName);
            } else {
                this.defenseUnits.put(typeName, defenseUnits.get(typeName));
            }
        }
    }

}
