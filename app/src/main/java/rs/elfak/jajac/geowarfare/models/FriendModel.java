package rs.elfak.jajac.geowarfare.models;

public class FriendModel {

    public String id;
    public String displayName;
    public String fullName;
    public String avatarUrl;

    public FriendModel() {
        // Default constructor required for calls to DataSnapshot.getValue(FriendModel.class)
    }

    public FriendModel(String id, String displayName, String fullName, String avatarUrl) {
        this.id = id;
        this.displayName = displayName;
        this.fullName = fullName;
        this.avatarUrl = avatarUrl;
    }

}
