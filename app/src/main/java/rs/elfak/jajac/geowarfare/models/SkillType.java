package rs.elfak.jajac.geowarfare.models;

import rs.elfak.jajac.geowarfare.R;

public enum SkillType {

    SCOUTING("Scouting", "Defines how far you can see other players and structures.", R.drawable.ic_eye, 10000, 3),
    DECEIT("Deceit", "Defines how accurately you can estimate defense army size in foreign structures.",
            R.drawable.ic_mask, 15000, 3);

    private String name;
    private String description;
    private int iconResourceId;
    private int baseCost;
    private int maxLevel;

    SkillType(String name, String description, int iconResourceId, int baseCost, int maxLevel) {
        this.name = name;
        this.description = description;
        this.iconResourceId = iconResourceId;
        this.baseCost = baseCost;
        this.maxLevel = maxLevel;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public int getIconResourceId() {
        return iconResourceId;
    }

    public int getBaseCost() {
        return baseCost;
    }

    public int getMaxLevel() {
        return maxLevel;
    }

    public boolean canUpgrade(int currentLevel) {
        return currentLevel < maxLevel;
    }

    public int getUpgradeCost(int currentLevel) {
        return currentLevel * currentLevel * baseCost;
    }
}
