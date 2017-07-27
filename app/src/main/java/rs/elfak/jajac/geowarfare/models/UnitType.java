package rs.elfak.jajac.geowarfare.models;

import rs.elfak.jajac.geowarfare.R;

public enum UnitType {

    SWORD("Knights", R.drawable.ic_sword, 500),
    BOW("Archers", R.drawable.ic_bow_and_arrow, 700);

    private String name;
    private int iconResourceId;
    private int baseCost;

    private UnitType(String name, int iconResourceId, int baseCost) {
        this.name = name;
        this.iconResourceId = iconResourceId;
        this.baseCost = baseCost;
    }

    public String getName() {
        return name;
    }

    public int getIconResourceId() {
        return iconResourceId;
    }

    public int getBaseCost() {
        return baseCost;
    }

}
