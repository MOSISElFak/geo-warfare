package rs.elfak.jajac.geowarfare.models;

public enum UnitType {

    KNIGHTS("Knights", 0),
    ARCHERS("Archers", 1);

    private String stringValue;
    private int intValue;

    private UnitType(String toString, int value){
        stringValue = toString;
        intValue = value;
    }

    @Override
    public String toString() {
        return stringValue;
    }

}
