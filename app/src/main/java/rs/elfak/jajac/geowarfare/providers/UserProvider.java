package rs.elfak.jajac.geowarfare.providers;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class UserProvider {

    private static UserProvider mInstance = null;

    private DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

    public static UserProvider getInstance() {
        if (mInstance == null) {
            mInstance = new UserProvider();
        }
        return mInstance;
    }

    private UserProvider() {
        // private constructor required for singleton
    }

    public DatabaseReference getUserById(String userId) {
        return mDatabase.child("users").child(userId);
    }

}
