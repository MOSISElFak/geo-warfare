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

    private String id;
    private String email;
    private String displayName;
    private String fullName;
    private String phone;
    private String avatarUrl;

    private CoordsModel coords;
    private Map<String, Boolean> friends = new HashMap<>();
    private Map<String, Boolean> friendRequests = new HashMap<>();
    private Map<String, Boolean> structures = new HashMap<>();
    private Map<String, Integer> units = new HashMap<>();
    private int gold = 0;

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
        this.coords = new CoordsModel(0, 0);
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

    @Exclude
    public int getUnitCount(UnitType type) {
        if (this.units.containsKey(type.toString())) {
            return this.units.get(type.toString());
        } else {
            return 0;
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public Map<String, Boolean> getFriends() {
        return friends;
    }

    public void setFriends(Map<String, Boolean> friends) {
        this.friends = friends;
    }

    public Map<String, Boolean> getFriendRequests() {
        return friendRequests;
    }

    public void setFriendRequests(Map<String, Boolean> friendRequests) {
        this.friendRequests = friendRequests;
    }

    public Map<String, Boolean> getStructures() {
        return structures;
    }

    public void setStructures(Map<String, Boolean> structures) {
        this.structures = structures;
    }

    public Map<String, Integer> getUnits() {
        Map<String, Integer> allUnits = new HashMap<>();

        for (UnitType unitType : UnitType.values()) {
            String typeName = unitType.toString();
            if (this.units.containsKey(typeName)) {
                allUnits.put(typeName, this.units.get(unitType.toString()));
            } else {
                allUnits.put(typeName, 0);
            }
        }
        return allUnits;
    }

    public void setUnits(Map<String, Integer> units) {
        for (UnitType unitType : UnitType.values()) {
            String typeName = unitType.toString();
            if (!units.containsKey(typeName) || units.get(typeName) == 0) {
                this.units.remove(typeName);
            } else {
                this.units.put(typeName, units.get(typeName));
            }
        }
    }

    public int getGold() {
        return gold;
    }

    public void setGold(int gold) {
        this.gold = gold;
    }


    public CoordsModel getCoords() {
        return coords;
    }

    public void setCoords(CoordsModel coords) {
        this.coords = coords;
    }

}
