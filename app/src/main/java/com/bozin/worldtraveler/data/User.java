package com.bozin.worldtraveler.data;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
    private String userID;
    private String userName;
    private Uri userPicturePath;

    public User (String userID, String userName, Uri userPicturePath){
        this.userID = userID;
        this.userName = userName;
        this.userPicturePath = userPicturePath;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.userID);
        dest.writeString(this.userName);
        dest.writeParcelable(this.userPicturePath, flags);
    }

    protected User(Parcel in) {
        this.userID = in.readString();
        this.userName = in.readString();
        this.userPicturePath = in.readParcelable(Uri.class.getClassLoader());
    }

    public static final Creator<User> CREATOR = new Creator<User>() {
        @Override
        public User createFromParcel(Parcel source) {
            return new User(source);
        }

        @Override
        public User[] newArray(int size) {
            return new User[size];
        }
    };
}
