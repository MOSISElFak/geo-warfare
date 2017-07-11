package rs.elfak.jajac.geowarfare.models;

public class CoordsModel {

    public double latitude;
    public double longitude;

    public CoordsModel() {
        // Default constructor required for calls to DataSnapshot.getValue(CoordsModel.class)
    }

    public CoordsModel(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }
}
