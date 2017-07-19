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
import java.util.Map;

import rs.elfak.jajac.geowarfare.models.UserModel;

public class UserProvider {

    private static UserProvider mInstance = null;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private DatabaseReference mUsersDatabase = FirebaseDatabase.getInstance().getReference().child("users");
    private StorageReference mAvatarsStorage = FirebaseStorage.getInstance().getReference().child("avatars");

    public static synchronized UserProvider getInstance() {
        if (mInstance == null) {
            mInstance = new UserProvider();
        }
        return mInstance;
    }

    private UserProvider() {
        // private constructor required for singleton
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

}
