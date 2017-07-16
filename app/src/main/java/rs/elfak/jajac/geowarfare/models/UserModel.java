package rs.elfak.jajac.geowarfare.models;

import android.os.Parcel;
import android.os.Parcelable;

public class UserModel implements Parcelable {

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

    private UserModel(Parcel in) {
        id = in.readString();
        email = in.readString();
        displayName = in.readString();
        fullName = in.readString();
        phone = in.readString();
        avatarUrl = in.readString();
    }

    public static final Creator<UserModel> CREATOR = new Creator<UserModel>() {
        @Override
        public UserModel createFromParcel(Parcel in) {
            return new UserModel(in);
        }

        @Override
        public UserModel[] newArray(int size) {
            return new UserModel[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(email);
        dest.writeString(displayName);
        dest.writeString(fullName);
        dest.writeString(phone);
        dest.writeString(avatarUrl);
    }
}
