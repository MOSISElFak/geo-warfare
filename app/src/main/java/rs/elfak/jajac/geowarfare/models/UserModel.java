package rs.elfak.jajac.geowarfare.models;

import com.google.firebase.database.Exclude;
import com.google.firebase.database.IgnoreExtraProperties;

import java.util.HashMap;
import java.util.Map;

@IgnoreExtraProperties
public class UserModel {

    public static final String KEY_USER_ID = "id";
    public static final String KEY_USER_EMAIL = "email";
    public static final String KEY_USER_DISPLAY_NAME = "displayName";
    public static final String KEY_USER_FULL_NAME = "fullName";
    public static final String KEY_USER_PHONE = "phone";
    public static final String KEY_USER_AVATAR_URL = "avatarUrl";

    public String id;
    public String email;
    public String displayName;
    public String fullName;
    public String phone;
    public String avatarUrl;
    public Map<String, Boolean> sentFriendRequests = new HashMap<>();
    public Map<String, Boolean> receivedFriendRequests = new HashMap<>();

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

    @Exclude
    public Map<String, Object> toMap() {
        HashMap<String, Object> result = new HashMap<>();
        result.put(KEY_USER_ID, id);
        result.put(KEY_USER_EMAIL, email);
        result.put(KEY_USER_DISPLAY_NAME, displayName);
        result.put(KEY_USER_FULL_NAME, fullName);
        result.put(KEY_USER_PHONE, phone);
        result.put(KEY_USER_AVATAR_URL, avatarUrl);

        return result;
    }

}
