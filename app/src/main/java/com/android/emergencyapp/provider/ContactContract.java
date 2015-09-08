package com.android.emergencyapp.provider;

import android.provider.BaseColumns;

/**
 * Created by Nirajan on 8/15/2015.
 */
public final class ContactContract {

    public ContactContract(){

    }

    public static abstract class ContactsEntry implements BaseColumns{
        // Contacts tables
        public static final String TABLE_CONTACTS = "contacts";
        // Table column names
        public static final String KEY_DISPLAY_NAME = "name";
        public static final String KEY_PHONE_NUMBER = "number";
        public static final String KEY_CONTACT_URI = "uri";

        // Indices tied to the table columns
        public static final int COL_CONTACT_ID = 0;
        public static final int COL_CONTACT_NAME = 1;
        public static final int COL_CONTACT_NUMBER = 2;
        public static final int COL_CONTACT_URI = 3;
    }
}
