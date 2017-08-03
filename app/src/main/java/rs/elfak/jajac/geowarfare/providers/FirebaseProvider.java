package rs.elfak.jajac.geowarfare.providers;

import android.net.Uri;

import com.firebase.geofire.GeoFire;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import rs.elfak.jajac.geowarfare.models.BarracksModel;
import rs.elfak.jajac.geowarfare.models.CoordsModel;
import rs.elfak.jajac.geowarfare.models.GoldMineModel;

public class FirebaseProvider {

    private static FirebaseProvider mInstance = null;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();

    // Firebase Realtime Database references
    private DatabaseReference mDbRef = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mUsersDbRef = mDbRef.child("users");
    private DatabaseReference mStructuresDbRef = mDbRef.child("structures");

    // Firebase Storage references
    private StorageReference mAvatarsStorageRef = FirebaseStorage.getInstance().getReference().child("avatars");

    // Geofire
    private GeoFire mUsersGeoFire = new GeoFire(mDbRef.child("usersGeoFire"));
    private GeoFire mStructuresGeoFire = new GeoFire(mDbRef.child("structuresGeoFire"));

    public static synchronized FirebaseProvider getInstance() {
        if (mInstance == null) {
            mInstance = new FirebaseProvider();
        }
        return mInstance;
    }

    private FirebaseProvider() {
        // private constructor required for singleton
    }

    public FirebaseAuth getAuthInstance() {
        return mAuth;
    }

    // *********************************************** USERS *********************************************** //

    public FirebaseUser getCurrentFirebaseUser() {
        return mAuth.getCurrentUser();
    }

    public DatabaseReference getUserById(String userId) {
        return mUsersDbRef.child(userId);
    }

    public DatabaseReference getCurrentUser() {
        return mUsersDbRef.child(mAuth.getCurrentUser().getUid());
    }

    public DatabaseReference getUserGold(String userId) {
        return mUsersDbRef.child(userId).child("gold");
    }

    public Task<AuthResult> createUserWithEmailAndPassword(String email, String password) {
        return mAuth.createUserWithEmailAndPassword(email, password);
    }

    public UploadTask uploadAvatarImage(String fileName, String localImgUri) {
        return mAvatarsStorageRef.child(fileName).putFile(Uri.fromFile(new File(localImgUri)));
    }

    public Task<Void> updateUserInfo(String userId, Map<String, Object> newUserValues) {
        return mUsersDbRef.child(userId).updateChildren(newUserValues);
    }

    public Task<Void> updateUserLocation(String userId, CoordsModel coords) {
        mUsersGeoFire.setLocation(userId, new GeoLocation(coords.getLatitude(), coords.getLongitude()));
        return mUsersDbRef.child(userId).child("coords").setValue(coords);
    }

    public DatabaseReference getReceivedFriendRequests(String toUserId) {
        return mUsersDbRef.child(toUserId).child("friendRequests");
    }

    public Task<Void> sendFriendRequest(String fromUserId, String toUserId) {
        return mUsersDbRef.child(toUserId).child("friendRequests").child(fromUserId).setValue(true);
    }

    public Task<Void> removeFriendRequest(String fromUserId, String toUserId) {
        return mUsersDbRef.child(toUserId).child("friendRequests").child(fromUserId).removeValue();
    }

    public Task<Void> addFriendship(String firstUserId, String secondUserId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(getUserFriendRequestPath(firstUserId, secondUserId), null);
        updates.put(getUserFriendRequestPath(secondUserId, firstUserId), null);
        updates.put(getUserFriendPath(firstUserId, secondUserId), true);
        updates.put(getUserFriendPath(secondUserId, firstUserId), true);

        return mUsersDbRef.updateChildren(updates);
    }

    public Task<Void> removeFriendship(String firstUserId, String secondUserId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(getUserFriendPath(firstUserId, secondUserId), null);
        updates.put(getUserFriendPath(secondUserId, firstUserId), null);

        return mUsersDbRef.updateChildren(updates);
    }

    public DatabaseReference getFriendRequestsForUser(String userId) {
        return mUsersDbRef.child(userId).child("friendRequests");
    }

    public GeoFire getUsersGeoFire() {
        return mUsersGeoFire;
    }

    private String getUserFriendPath(String userId, String friendUserId) {
        return "/" + userId + "/friends/" + friendUserId;
    }

    private String getUserFriendRequestPath(String fromUserId, String toUserId) {
        return "/" + toUserId + "/friendRequests/" + fromUserId;
    }

    // ********************************************* STRUCTURES ********************************************* //

    public DatabaseReference getStructureById(String structureId) {
        return mStructuresDbRef.child(structureId);
    }

    public Task<Void> addGoldMine(GoldMineModel goldMine, CoordsModel coords, int newUserGoldValue) {
        String newStructureKey = mStructuresDbRef.push().getKey();

        Map<String, Object> updates = new HashMap<>();
        updates.put("/structures/" + newStructureKey, goldMine);
        updates.put("/users/" + goldMine.getOwnerId() + "/structures/" + newStructureKey, true);
        updates.put("/users/" + goldMine.getOwnerId() + "/gold", newUserGoldValue);

        GeoLocation geoLoc = new GeoLocation(coords.getLatitude(), coords.getLongitude());
        mStructuresGeoFire.setLocation(newStructureKey, geoLoc);

        return mDbRef.updateChildren(updates);
    }

    public Task<Void> addBarracks(BarracksModel barracks, CoordsModel coords, int newUserGoldValue) {
        String newStructureKey = mStructuresDbRef.push().getKey();

        Map<String, Object> updates = new HashMap<>();
        updates.put("/structures/" + newStructureKey, barracks);
        updates.put("/users/" + barracks.getOwnerId() + "/structures/" + newStructureKey, true);
        updates.put("/users/" + barracks.getOwnerId() + "/gold", newUserGoldValue);

        GeoLocation geoLoc = new GeoLocation(coords.getLatitude(), coords.getLongitude());
        mStructuresGeoFire.setLocation(newStructureKey, geoLoc);

        return mDbRef.updateChildren(updates);
    }

    public GeoFire getStructuresGeoFire() {
        return mStructuresGeoFire;
    }

    public Task<Void> takeGold(String userId, int userTotalGoldAmount, String fromGoldMineId) {
        Map<String, Object> updates = new HashMap<>();

        updates.put("/structures/" + fromGoldMineId + "/gold", 0);
        updates.put("/users/" + userId + "/gold", userTotalGoldAmount);

        return mDbRef.updateChildren(updates);
    }

    public Task<Void> upgradeStructureLevel(String userId, int newGoldAmount,
                                            String structureId, int newStructureLevel) {
        Map<String, Object> updates = new HashMap<>();

        updates.put("/structures/" + structureId + "/level", newStructureLevel);
        updates.put("/users/" + userId + "/gold", newGoldAmount);

        return mDbRef.updateChildren(updates);
    }

    public Task<Void> transferUnits(String structureId, Map<String, Integer> newStructureUnits,
                                    String userId, Map<String, Integer> newUserUnits) {
        Map<String, Object> updates = new HashMap<>();

        updates.put("/structures/" + structureId + "/defenseUnits", newStructureUnits);
        updates.put("/users/" + userId + "/units", newUserUnits);

        return mDbRef.updateChildren(updates);
    }

    public Task<Void> purchaseUnits(String userId, Map<String, Integer> newUserUnits,
                                    String barracksId, Map<String, Integer> newBarracksUnits)
    {
        Map<String, Object> updates = new HashMap<>();

        updates.put("/users/" + userId + "/units", newUserUnits);
        updates.put("/structures/" + barracksId + "/availableUnits", newBarracksUnits);

        return mDbRef.updateChildren(updates);
    }

}
