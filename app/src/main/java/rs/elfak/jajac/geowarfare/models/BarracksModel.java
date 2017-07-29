package rs.elfak.jajac.geowarfare.models;

import com.google.firebase.database.Exclude;

import java.util.HashMap;
import java.util.Map;

public class BarracksModel extends StructureModel {

    private Map<String, Integer> availableUnits = new HashMap<>();

    public BarracksModel() {
        // Default constructor required for calls to DataSnapshot.getValue(BarracksModel.class)
    }

    public BarracksModel(StructureType type, String ownerId) {
        super(type, ownerId);
        Map<UnitType, Integer> firstLevelAvailable = this.getCurrentAvailable();

        for (UnitType unitType : firstLevelAvailable.keySet()) {
            this.availableUnits.put(unitType.toString(), firstLevelAvailable.get(unitType));
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

    @Exclude @Override
    public int getUpgradeCost() {
        return getCostForLevel(this.level + 1);
    }

    @Exclude @Override
    protected int getCostForLevel(int level) {
        if (level == 1) {
            return this.type.getBaseCost();
        } else {
            return level * level * getCostForLevel(level - 1);
        }
    }

    public Map<String, Integer> getAvailableUnits() {
        Map<String, Integer> allUnits = new HashMap<>();

        for (UnitType unitType : UnitType.values()) {
            String typeName = unitType.toString();
            if (this.availableUnits.containsKey(typeName)) {
                allUnits.put(typeName, this.availableUnits.get(unitType.toString()));
            } else {
                allUnits.put(typeName, 0);
            }
        }
        return allUnits;
    }

    public void setAvailableUnits(Map<String, Integer> availableUnits) {
        for (UnitType unitType : UnitType.values()) {
            String typeName = unitType.toString();
            if (!availableUnits.containsKey(typeName) || availableUnits.get(typeName) == 0) {
                this.availableUnits.remove(typeName);
            } else {
                this.availableUnits.put(typeName, availableUnits.get(typeName));
            }
        }
    }

}
