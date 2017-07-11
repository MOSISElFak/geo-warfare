package rs.elfak.jajac.geowarfare.models;

public class UserModel {

    public String id;
    public String email;
    public String displayName;
    public String fullName;
    public String phone;
    public String avatarUrl;
    public CoordsModel coordinates = new CoordsModel(0.0, 0.0);

    public UserModel() {
        // Default constructor required for calls to DataSnapshot.getValue(UserModel.class)
    }

    public UserModel(String id, String email, String displayName, String fullName, String phone, String imgUrl) {
        this.id = id;
        this.email = email;
        this.displayName = displayName;
        this.fullName = fullName;
        this.phone = phone;
        this.avatarUrl = imgUrl;
    }

}
