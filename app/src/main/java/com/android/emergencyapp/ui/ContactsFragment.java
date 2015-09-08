package com.android.emergencyapp.ui;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;

import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ListView;

import com.android.emergencyapp.R;
import com.android.emergencyapp.model.Contact;
import com.android.emergencyapp.provider.ContactContract;
import com.android.emergencyapp.provider.ContactDatabaseHelper;
import com.android.emergencyapp.ui.adapter.ContactsAdapter;
import com.android.emergencyapp.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;


/**
 * Created by Nirajan on 8/2/2015.
 */
public class ContactsFragment extends Fragment {
    private static final String TAG = "ContactsFragment";

    private static final int RESULT_PICK_CONTACT = 99;

    // Current action mode (contextual action bar, a.k.a. CAB)
    private ActionMode mActionMode;

    // Store all the emergency contacts
    private List<Contact> allContacts;

    // Floating action button to add emergency contacts
    private ImageButton addContactsButton;

    //private ContactsRecyclerViewAdapter myAdapter;
    private ListView contactsListView;
    private ContactsAdapter myAdapter;

    // Selection projection that uses all columns from the table
    private String[] mAllColumns = { ContactContract.ContactsEntry._ID,
            ContactContract.ContactsEntry.KEY_DISPLAY_NAME,
            ContactContract.ContactsEntry.KEY_PHONE_NUMBER};


    public static ContactsFragment newInstance(){
        ContactsFragment fragment = new ContactsFragment();
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        return inflater.inflate(R.layout.fragment_contacts, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        addContactsButton = (ImageButton) view.findViewById(R.id.fab_add);
        addContactsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent contactPickerIntent = new Intent(Intent.ACTION_PICK,
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI);
                // use getParentFragment() since this fragment is hosted in another fragment (MainFragment)
                if (contactPickerIntent.resolveActivity(getActivity().getPackageManager()) != null) {
                    getParentFragment().startActivityForResult(contactPickerIntent, RESULT_PICK_CONTACT);
                }

            }
        });

        contactsListView = (ListView) view.findViewById(R.id.contacts_listview);
        // Get all the emergency contacts saved
        allContacts = new ArrayList<Contact>();
        allContacts = ContactDatabaseHelper.getInstance(getActivity()).getAllContacts();

        for (Contact c: allContacts)
            Log.d(TAG, c.toString());

        myAdapter = new ContactsAdapter(getActivity(), allContacts);
        contactsListView.setAdapter(myAdapter);
        contactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "Item Click");
            }
        });
        contactsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Log.d(TAG, "onItemLongClick");
                //createAlertDialog(position);
                if (mActionMode == null) {
                    mActionMode = getActivity().startActionMode(mActionModeCallback);
                    myAdapter.toggleSelection(position);
                    myAdapter.notifyDataSetChanged();
                }
                return true;
            }
        });


        // Click listeners for the recyclerview adapter items
       /* myAdapter.setOnItemClickListener(new ContactsRecyclerViewAdapter.onItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

            }

            @Override
            public void onItemLongClick(View view, int position) {
                Log.d(TAG, "onItemLongClick");
                //createAlertDialog(position);
                if (mActionMode == null) {
                    mActionMode = getActivity().startActionMode(mActionModeCallback);
                    Contact clickedContact = myAdapter.getItem(position);
                    clickedContact.setIsSelected(true);
                    myAdapter.notifyDataSetChanged();
                }
            }
        });*/

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
    }

    /**
     * Creates AlertDialog to display more options
     */
    private void createAlertDialog(final int position){
        final int index = position;
        AlertDialog.Builder builder1 = new AlertDialog.Builder(getActivity());
        String options[] = { "View Contact", "Delete Contact"};
        builder1.setCancelable(true)
                .setItems(options, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (which == 0){
                            Log.d(TAG, "View contact clicked");
                            Log.d(TAG, "Starting contact view, URI: " + myAdapter.getItem(index).getUri().toString());

                            Intent intent = new Intent(Intent.ACTION_VIEW, myAdapter.getItem(index).getUri());
                            getActivity().startActivity(intent);
                            /*if (intent.resolveActivity(getActivity().getPackageManager()) != null) {
                                getActivity().startActivity(intent);
                            }*/

                        } else if (which == 1){
                            deleteEmergencyContact(myAdapter.getItem(index), index);
                        }
                    }
                });
        AlertDialog alert1= builder1.create();
        alert1.show();
    }


    /**
     * Adds the selected contact to the emergency contacts database table
     * @param contact
     */
    private void addEmergencyContact(Contact contact){
        long insertId = ContactDatabaseHelper.getInstance(getActivity()).addContact(contact);
        Log.d(TAG, "Insert contact ID: " + String.valueOf(insertId));
        if (insertId > 0){
            Log.e(TAG, "Unable to add the contact to the table");
            myAdapter.addItem(contact);
            // sort the list alphabetically
            Collections.sort(myAdapter.getList(), new Comparator<Contact>() {
                @Override
                public int compare(Contact lhs, Contact rhs) {
                    return lhs.getDisplayName().compareTo(rhs.getDisplayName());
                }
            });
            myAdapter.notifyDataSetChanged();
        }
    }

    /**
     * Deletes the selected contact to the emergency contacts database table
     * @param contact
     */
    private void deleteEmergencyContact(Contact contact, int position){
        ContactDatabaseHelper.getInstance(getActivity()).deleteContact(contact);
        myAdapter.removeItem(position);
        myAdapter.notifyDataSetChanged();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult");
        if (requestCode == RESULT_PICK_CONTACT){
            if (resultCode == Activity.RESULT_OK){
                Log.i(TAG, "Contact Picked: " + data.toString());
                // the URI returned from selecting a contact
                Uri contactData = data.getData();
                // Set up the projection to get display name and phone number
                String[] projection = new String[] {ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                        ContactsContract.CommonDataKinds.Phone.NUMBER};
                // Set up the cursor
                Cursor contact = getActivity().getContentResolver().query(contactData, projection, null,
                        null, null);
                int indexName = contact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME);
                int indexNumber = contact.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER);
                contact.moveToFirst();
                do {
                    String name   = contact.getString(indexName);
                    Log.d(TAG, "DISPLAY NAME: " + name);
                    String number = contact.getString(indexNumber);
                    Log.d(TAG, "PHONE: " + number);
                    // Create a new contact model
                    Contact person = new Contact(name, number, contactData);
                    addEmergencyContact(person);
                } while (contact.moveToNext());
            }
        }
    }



    private ActionMode.Callback mActionModeCallback = new ActionMode.Callback(){

        // Called when the action mode is created; startActionMode() was called
        @Override
        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            // Inflate a menu resource providing context menu items
            MenuInflater inflater = mode.getMenuInflater();
            inflater.inflate(R.menu.context_menu, menu);
            return true;
        }

        // Called each time the action mode is shown. Always called after onCreateActionMode, but
        // may be called multiple times if the mode is invalidated.
        @Override
        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            mode.setTitle("0 selected");
            return false;
        }

        @Override
        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()){
                case R.id.menu_delete:
                    Log.d(TAG, "Menu Delete clicked");

                    return true;
                default:
                    return false;
            }
        }

        @Override
        public void onDestroyActionMode(ActionMode mode) {
            mActionMode = null;
        }
    };

}
