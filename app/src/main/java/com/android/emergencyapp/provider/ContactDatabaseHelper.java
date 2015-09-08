package com.android.emergencyapp.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.android.emergencyapp.model.Contact;
import com.android.emergencyapp.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Nirajan on 8/14/2015.
 */
public class ContactDatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "ContactDatabaseHelper";

    // Database version
    private static final int DATABASE_VERSION = 1;
    // Database name
    private static final String DATABASE_NAME = "emergencyContacts";

    // Statement to execute to create the contacts table
    private static final String CREATE_TABLE_CONTACTS = "CREATE TABLE " + ContactContract.ContactsEntry.TABLE_CONTACTS + " ("
            + ContactContract.ContactsEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + ContactContract.ContactsEntry.KEY_DISPLAY_NAME + " TEXT NOT NULL,"
            + ContactContract.ContactsEntry.KEY_PHONE_NUMBER + " TEXT NOT NULL,"
            + ContactContract.ContactsEntry.KEY_CONTACT_URI + " TEXT NOT NULL,"
            + "UNIQUE (" + ContactContract.ContactsEntry.KEY_PHONE_NUMBER + ") )";

    // Statement to execute to drop the contacts table
    private static final String DROP_TABLE_CONTACTS = "DROP TABLE IF EXISTS " + ContactContract.ContactsEntry.TABLE_CONTACTS;

    // Selection projection that uses all columns from the table
    private String[] mAllColumns = { ContactContract.ContactsEntry._ID,
                        ContactContract.ContactsEntry.KEY_DISPLAY_NAME,
                        ContactContract.ContactsEntry.KEY_PHONE_NUMBER,
                        ContactContract.ContactsEntry.KEY_CONTACT_URI};

    // Static instance of the Database helper
    private static ContactDatabaseHelper mInstance;

    /**
    * Singleton pattern instantiate the database helper class
     * @return ContactDatabaseHelper
    */
    public static synchronized ContactDatabaseHelper getInstance(Context context){
        if (mInstance == null){
            mInstance = new ContactDatabaseHelper(context.getApplicationContext());
        }
        return mInstance;
    }

    /**
     * Private constructor to avoid direct instantiation
     * @param context
     */
    private ContactDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d(TAG, "onCreate() EmergencyContacts table ");
        db.execSQL(CREATE_TABLE_CONTACTS);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.d(TAG, "onUpgrade() from " + oldVersion + " to " + newVersion);
        db.execSQL(DROP_TABLE_CONTACTS);
        onCreate(db);
    }

    /**
     * Insert a row of contact in the table
     * @param contact
     */
    public long addContact(Contact contact){
        // Create and/or open database for insert
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(ContactContract.ContactsEntry.KEY_DISPLAY_NAME, contact.getDisplayName());
        values.put(ContactContract.ContactsEntry.KEY_PHONE_NUMBER, contact.getPhoneNumber());
        values.put(ContactContract.ContactsEntry.KEY_CONTACT_URI, contact.getUri().toString());

        long newRowId = -1;
        db.beginTransaction();
        try{
            newRowId = db.insert(ContactContract.ContactsEntry.TABLE_CONTACTS, null, values);
            db.setTransactionSuccessful();
        } catch (Exception e){
            e.printStackTrace();
        } finally {
            db.endTransaction();
        }
        return newRowId;
        //return db.insert(ContactsContract.ContactsEntry.TABLE_CONTACTS, null, values);
       /* return db.insertWithOnConflict(ContactsContract.ContactsEntry.TABLE_CONTACTS, null,
                values, SQLiteDatabase.CONFLICT_REPLACE);*/
    }

    /**
     * Delete a row of contact
     * @param contact
     */
    public void deleteContact(Contact contact){
        // Create and/or open database for insert
        SQLiteDatabase db = this.getWritableDatabase();

        String selection = ContactContract.ContactsEntry.KEY_PHONE_NUMBER + " LIKE ?";
        String[] selectionArgs = {contact.getPhoneNumber()};

        db.beginTransaction();
        try{
            db.delete(ContactContract.ContactsEntry.TABLE_CONTACTS, selection, selectionArgs);
            db.setTransactionSuccessful();
        } catch (Exception e){
            e.printStackTrace();
            Log.e(TAG, "Error deleting contact");
        } finally {
            db.endTransaction();
        }


    }

    public Cursor getAllContactsCursor(){
        List<Contact> contactList = new ArrayList<Contact>();
        SQLiteDatabase db = this.getReadableDatabase();
        // Sort order
        String sortOrder = ContactContract.ContactsEntry.KEY_DISPLAY_NAME + " ASC";
        Cursor cursor = db.query(
                ContactContract.ContactsEntry.TABLE_CONTACTS,
                mAllColumns,
                null, null, null, null, null
        );

        return cursor;
    }

    /**
     * Retrieves all the contacts stored in the table
     *
     */
    public List<Contact> getAllContacts(){
        List<Contact> contactList = new ArrayList<Contact>();
        SQLiteDatabase db = this.getReadableDatabase();
        // Sort order
        String sortOrder = ContactContract.ContactsEntry.KEY_DISPLAY_NAME + " ASC";
        Cursor cursor = db.query(
                ContactContract.ContactsEntry.TABLE_CONTACTS,
                mAllColumns,
                null, null, null, null, sortOrder
        );

        if (cursor.moveToFirst()) {
            do{
                Contact contact = new Contact(cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getString(2),
                        Uri.parse(cursor.getString(3)));
                contactList.add(contact);
            } while (cursor.moveToNext());
        }
        return contactList;
    }
}
