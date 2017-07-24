package rs.elfak.jajac.geowarfare.models;

import java.util.Map;

public class GoldMineModel extends StructureModel {

    public static final int COST = 500;

    public int gold = 0;

    public GoldMineModel(StructureType type, String ownerId) {
        super(type, ownerId);
    }

}
