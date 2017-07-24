package rs.elfak.jajac.geowarfare.models;

import rs.elfak.jajac.geowarfare.R;

public enum StructureType {

    // The string names are for views in the app
    GOLD_MINE("Gold mine", 0, R.drawable.ic_gold_cart),
    BARRACKS("Barracks", 1, R.drawable.ic_barracks);

    private String name;
    private int value;
    private int iconResourceId;

    StructureType(String name, int value, int iconResourceId){
        this.name = name;
        this.value = value;
        this.iconResourceId = iconResourceId;
    }

    public String getName() {
        return this.name;
    }

    public int getIconResourceId() {
        return this.iconResourceId;
    }

}
