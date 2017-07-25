package rs.elfak.jajac.geowarfare.models;

import rs.elfak.jajac.geowarfare.R;

public enum StructureType {

    // The string names are for views in the app
    GOLD_MINE("Gold mine", 0, R.drawable.ic_gold_cart, 3),
    BARRACKS("Barracks", 1, R.drawable.ic_barracks, 3);

    private String name;
    private int value;
    private int iconResourceId;
    private int maxLevel;

    StructureType(String name, int value, int iconResourceId, int maxLevel) {
        this.name = name;
        this.value = value;
        this.iconResourceId = iconResourceId;
        this.maxLevel = maxLevel;
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

}
