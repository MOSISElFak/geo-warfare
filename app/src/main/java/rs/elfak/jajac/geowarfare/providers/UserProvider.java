package rs.elfak.jajac.geowarfare.providers;

import rs.elfak.jajac.geowarfare.models.UserModel;

public class UserProvider {

    private static UserProvider mInstance = null;

    public static UserProvider getmInstance() {
        if (mInstance == null) {
            mInstance = new UserProvider();
        }
        return mInstance;
    }

    private UserProvider() {
        // needed for singleton
    }

    public void setUser(UserModel user) {

    }

}
