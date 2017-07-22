package rs.elfak.jajac.geowarfare.models;

import java.util.HashMap;
import java.util.Map;

public abstract class StructureModel {

    public String id;
    public String type;
    public int level = 1;
    public Map<String, Integer> defense = new HashMap<>();

    public StructureModel() {
        // Default constructor required for calls to DataSnapshot.getValue(StructureModel.class)
    }

    public StructureModel(String id, String type, int level, Map<String, Integer> defense) {
        this.id = id;
        this.type = type;
        this.level = level;
        this.defense = defense;
    }

}
