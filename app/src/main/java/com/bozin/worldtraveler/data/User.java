package com.bozin.worldtraveler.data;

import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
    private String userName;
    private Uri userPicturePath;

    public User (String userName, Uri userPicturePath){
        this.userName = userName;
        this.userPicturePath = userPicturePath;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Uri getUserPicturePath() {
        return userPicturePath;
    }

    public void setUserPicturePath(Uri userPicturePath) {
        this.userPicturePath = userPicturePath;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.userName);
        dest.writeParcelable(this.userPicturePath, flags);
    }

    protected User(Parcel in) {
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
