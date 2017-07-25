package rs.elfak.jajac.geowarfare.models;

import rs.elfak.jajac.geowarfare.R;

public enum StructureType {

    // The string names are for views in the app
    GOLD_MINE("Gold mine", 0, R.drawable.ic_gold_cart, 3, 250),
    BARRACKS("Barracks", 1, R.drawable.ic_barracks, 3, 1000);

    private String name;
    private int value;
    private int iconResourceId;
    private int maxLevel;
    private int baseCost;

    StructureType(String name, int value, int iconResourceId, int maxLevel, int baseCost) {
        this.name = name;
        this.value = value;
        this.iconResourceId = iconResourceId;
        this.maxLevel = maxLevel;
        this.baseCost = baseCost;
    }

    public String getName() {
        return this.name;
    }

    public int getIconResourceId() {
        return this.iconResourceId;
    }

    public int getMaxLevel() {
        return this.maxLevel;
    }

    public int getBaseCost() {
        return this.baseCost;
    }

}
