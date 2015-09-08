package com.android.emergencyapp.model;

import android.net.Uri;

/**
 * Class that represents an emergency contact
 *
 * Created by Nirajan on 8/14/2015.
 */
public class Contact {

    private int _id;
    private String displayName;
    private String phoneNumber;
    private Uri uri;

    public Contact(String displayName, String phoneNumber, Uri uri) {
        this.displayName = displayName;
        this.phoneNumber = phoneNumber;
        this.uri = uri;
    }

    public Contact(int id, String displayName, String phoneNumber, Uri uri) {
        this._id = id;
        this.displayName = displayName;
        this.phoneNumber = phoneNumber;
        this.uri = uri;
    }

    public int get_id() {
        return _id;
    }

    public void set_id(int _id) {
        this._id = _id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Uri getUri() {
        return uri;
    }

    public void setUri(Uri uri) {
        this.uri = uri;
    }

    @Override
    public String toString() {
        return "Contact{" +
                "_id=" + _id +
                ", displayName='" + displayName + '\'' +
                ", phoneNumber='" + phoneNumber + '\'' +
                ", uri='" + uri + '\'' +
                '}';
    }
}
