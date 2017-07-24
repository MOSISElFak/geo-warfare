package rs.elfak.jajac.geowarfare.models;

public enum StructureType {

    // The string names are for views in the app
    GOLD_MINE("Gold mine", 0),
    BARRACKS("Barracks", 1);

    private String name;
    private int value;

    StructureType(String name, int value){
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

}
