package com.bozin.worldtraveler.data;


import android.os.Parcel;
import android.os.Parcelable;

public class User implements Parcelable {
    private String uuid;
    private String userName;
    private String userPicturePath;
    private String friendsList;




    public User (String uuid, String userName, String userPicturePath, String friendsList){
        this.uuid = uuid;
        this.userName = userName;
        this.userPicturePath = userPicturePath;
        this.friendsList = friendsList;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserPicturePath() {
        return userPicturePath;
    }

    public void setUserPicturePath(String userPicturePath) {
        this.userPicturePath = userPicturePath;
    }

    public String getFriendsList() {
        return friendsList;
    }

    public void setFriendsList(String friendsList) {
        this.friendsList = friendsList;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.uuid);
        dest.writeString(this.userName);
        dest.writeString(this.userPicturePath);
        dest.writeString(this.friendsList);
    }

    protected User(Parcel in) {
        this.uuid = in.readString();
        this.userName = in.readString();
        this.userPicturePath = in.readString();
        this.friendsList = in.readString();
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
