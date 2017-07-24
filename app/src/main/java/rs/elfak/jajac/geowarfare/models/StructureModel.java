package rs.elfak.jajac.geowarfare.models;

import java.util.HashMap;
import java.util.Map;

public class StructureModel {

    public String id;
    public String ownerId;
    public StructureType type;
    public int level = 1;
    public Map<String, Integer> defense = new HashMap<>();

    public StructureModel() {
        // Default constructor required for calls to DataSnapshot.getValue(StructureModel.class)
    }

    public StructureModel(StructureType type, String ownerId) {
        this.type = type;
        this.ownerId = ownerId;
    }

}
