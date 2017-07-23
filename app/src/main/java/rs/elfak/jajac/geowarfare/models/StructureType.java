package rs.elfak.jajac.geowarfare.models;

public enum StructureType {

    GOLD_MINE("Gold mine", 0),
    BARRACKS("Barracks", 1);

    private String name;
    private int value;

    private StructureType(String name, int value){
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return this.name;
    }

    public int getValue() {
        return this.value;
    }

}
