package rs.elfak.jajac.geowarfare.models;

import rs.elfak.jajac.geowarfare.R;

public enum StructureType {

    // The string names are for views in the app
    GOLD_MINE("Gold mine", R.drawable.ic_gold_cart, 3, 250, true),
    BARRACKS("Barracks", R.drawable.ic_barracks, 3, 1000, true);

    private String name;
    private int iconResId;
    private int maxLevel;
    private int baseCost;
    private boolean hasDefense;

    StructureType(String name, int iconResourceId, int maxLevel, int baseCost, boolean hasDefense) {
        this.name = name;
        this.iconResId = iconResourceId;
        this.maxLevel = maxLevel;
        this.baseCost = baseCost;
        this.hasDefense = hasDefense;
    }

    public String getName() {
        return this.name;
    }

    public int getIconResId() {
        return this.iconResId;
    }

    public int getMaxLevel() {
        return this.maxLevel;
    }

    public int getBaseCost() {
        return this.baseCost;
    }

    public boolean hasDefense() {
        return this.hasDefense;
    }

}
