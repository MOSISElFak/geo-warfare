package rs.elfak.jajac.geowarfare.models;

import rs.elfak.jajac.geowarfare.R;

public enum UnitType {

    SWORD("Knights", R.drawable.ic_sword, 500, 7, 5),
    BOW("Archers", R.drawable.ic_bow_and_arrow, 700, 5, 7);

    private String name;
    private int iconResourceId;
    private int baseCost;
    private int offensePower;
    private int defensePower;

    private UnitType(String name, int iconResourceId, int baseCost, int attackPower, int defendPower) {
        this.name = name;
        this.iconResourceId = iconResourceId;
        this.baseCost = baseCost;
        this.offensePower = attackPower;
        this.defensePower = defendPower;
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

    public int getOffensePower() {
        return offensePower;
    }

    public int getDefensePower() {
        return defensePower;
    }
}
