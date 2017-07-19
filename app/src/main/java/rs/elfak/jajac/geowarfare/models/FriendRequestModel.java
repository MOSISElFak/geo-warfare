package rs.elfak.jajac.geowarfare.models;

public class FriendRequestModel {

    public String id;
    public String displayName;
    public String fullName;
    public String avatarUrl;

    public FriendRequestModel() {
        // Default constructor required for calls to DataSnapshot.getValue(FriendRequestModel.class)
    }

    public FriendRequestModel(String id, String displayName, String fullName, String avatarUrl) {
        this.id = id;
        this.displayName = displayName;
        this.fullName = fullName;
        this.avatarUrl = avatarUrl;
    }

}
