package rs.elfak.jajac.geowarfare.providers;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;

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

import rs.elfak.jajac.geowarfare.models.GoldMineModel;
import rs.elfak.jajac.geowarfare.models.UserModel;

public class UserProvider {

    private static UserProvider mInstance = null;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
    private DatabaseReference mUsersDatabase = mDatabase.child("users");
    private StorageReference mAvatarsStorage = FirebaseStorage.getInstance().getReference().child("avatars");

    private DatabaseReference mStructuresDatabase = mDatabase.child("structures");

    public static synchronized UserProvider getInstance() {
        if (mInstance == null) {
            mInstance = new UserProvider();
        }
        return mInstance;
    }

    private UserProvider() {
        // private constructor required for singleton
    }

    public FirebaseAuth getAuthInstance() {
        return mAuth;
    }

    public FirebaseUser getCurrentUser() {
        return mAuth.getCurrentUser();
    }

    public DatabaseReference getUserById(String userId) {
        return mUsersDatabase.child(userId);
    }

    public Task<AuthResult> createUserWithEmailAndPassword(String email, String password) {
        return mAuth.createUserWithEmailAndPassword(email, password);
    }

    public UploadTask uploadAvatarImage(String fileName, String localImgUri) {
        return mAvatarsStorage.child(fileName).putFile(Uri.fromFile(new File(localImgUri)));
    }

    public Task<Void> removeAvatarImage(String storageImgUri) {
        return mAvatarsStorage.child(storageImgUri).delete();
    }

    public Task<Void> updateUserInfo(String userId, Map<String, Object> newUserValues) {
        return mUsersDatabase.child(userId).updateChildren(newUserValues);
    }

    public DatabaseReference getReceivedFriendRequests(String toUserId) {
        return mUsersDatabase.child(toUserId).child("friendRequests");
    }

    public Task<Void> sendFriendRequest(String fromUserId, String toUserId) {
        return mUsersDatabase.child(toUserId).child("friendRequests").child(fromUserId).setValue(true);
    }

    public Task<Void> removeFriendRequest(String fromUserId, String toUserId) {
        return mUsersDatabase.child(toUserId).child("friendRequests").child(fromUserId).removeValue();
    }

    public Task<Void> addFriendship(String firstUserId, String secondUserId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(getUserFriendRequestPath(firstUserId, secondUserId), null);
        updates.put(getUserFriendRequestPath(secondUserId, firstUserId), null);
        updates.put(getUserFriendPath(firstUserId, secondUserId), true);
        updates.put(getUserFriendPath(secondUserId, firstUserId), true);

        return mUsersDatabase.updateChildren(updates);
    }

    public Task<Void> removeFriendship(String firstUserId, String secondUserId) {
        Map<String, Object> updates = new HashMap<>();
        updates.put(getUserFriendPath(firstUserId, secondUserId), null);
        updates.put(getUserFriendPath(secondUserId, firstUserId), null);

        return mUsersDatabase.updateChildren(updates);
    }

    public DatabaseReference getFriendRequestsForUser(String userId) {
        return mUsersDatabase.child(userId).child("friendRequests");
    }

    private String getUserFriendPath(String userId, String friendUserId) {
        return "/" + userId + "/friends/" + friendUserId;
    }

    private String getUserFriendRequestPath(String fromUserId, String toUserId) {
        return "/" + toUserId + "/friendRequests/" + fromUserId;
    }

    public Task<Void> addGoldMine(GoldMineModel goldMine) {
        String newStructureKey = mStructuresDatabase.push().getKey();

        Map<String, Object> updates = new HashMap<>();
        updates.put("/structures/" + newStructureKey, goldMine);
        updates.put("/users/" + goldMine.ownerId + "/structures/" + newStructureKey, true);

        return mDatabase.updateChildren(updates);
    }

}
