package com.android.emergencyapp.ui.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.android.emergencyapp.R;
import com.android.emergencyapp.provider.ContactContract;
import com.android.emergencyapp.ui.widget.CircularImageView;

/**
 * Created by Nirajan on 8/17/2015.
 */
public class ContactsCursorAdapter extends CursorAdapter {

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CircularImageView imageView;
        public TextView displayName;
        public TextView phoneNumber;

        public ViewHolder(View v) {
            super(v);
            this.imageView = (CircularImageView) v.findViewById(R.id.contact_icon);
            this.displayName = (TextView) v.findViewById(R.id.contact_name);
            this.phoneNumber = (TextView) v.findViewById(R.id.contact_phone);
        }
    }

    public ContactsCursorAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.contacts_list, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder holder = (ViewHolder) view.getTag();
        String name = cursor.getString(ContactContract.ContactsEntry.COL_CONTACT_NAME);
        holder.displayName.setText(name);
        holder.imageView.setLetter(name.charAt(0));
        holder.phoneNumber.setText(cursor.getString(ContactContract.ContactsEntry.COL_CONTACT_NUMBER));
    }
}
