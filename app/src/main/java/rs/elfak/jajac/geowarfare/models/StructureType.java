package rs.elfak.jajac.geowarfare.models;

public enum StructureType {

    GOLD_MINE("Gold mine", 0),
    BARRACKS("Barracks", 1);

    private String stringValue;
    private int intValue;

    private StructureType(String toString, int value){
        stringValue = toString;
        intValue = value;
    }

    @Override
    public String toString() {
        return stringValue;
    }

}
