package rs.elfak.jajac.geowarfare.models;

import java.util.Map;

public class GoldMineModel extends StructureModel {

    public static final int COST = 500;

    public int gold = 0;

    public GoldMineModel(String id, String type, Integer level, Map<String, Integer> defense) {
        super(id, type, level, defense);
    }

}
