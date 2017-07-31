package rs.elfak.jajac.geowarfare.models;

public class MarkerTag {

    private Object object;
    private double latitude;
    private double longitude;

    public MarkerTag(Object object, double latitude, double longitude) {
        this.object = object;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public Object getObject() {
        return object;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

}
