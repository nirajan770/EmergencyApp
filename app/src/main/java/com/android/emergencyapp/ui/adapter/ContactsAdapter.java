package com.android.emergencyapp.ui.adapter;

import android.content.Context;
import android.support.v4.app.TaskStackBuilder;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.emergencyapp.R;
import com.android.emergencyapp.model.Contact;
import com.android.emergencyapp.ui.widget.CircularImageView;

import java.util.List;

/**
 * Created by Nirajan on 8/28/2015.
 */
public class ContactsAdapter extends BaseAdapter {

    private static final String TAG = "ContactsAdapter";

    private Context mContext;
    private List<Contact> data;

    // keep track of selected items
    private SparseBooleanArray mSelectedItemsIds;

    public static class ViewHolder{
        public RelativeLayout rootView;
        public CircularImageView imageView;
        public TextView displayName;
        public TextView phoneNumber;
    }

    public ContactsAdapter(Context context, List<Contact> data) {
        this.mContext = context;
        this.data = data;
        mSelectedItemsIds = new SparseBooleanArray();
    }


    @Override
    public int getCount() {
        return data.size();
    }

    @Override
    public Contact getItem(int position) {
        return data.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public List<Contact> getList(){
        return data;
    }

    public void addItem(Contact contact){
        data.add(contact);
    }

    public void removeItem(int position){
        data.remove(position);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null){
            holder = new ViewHolder();
            convertView = LayoutInflater.from(mContext).inflate(R.layout.contacts_list, parent, false);
            holder.rootView = (RelativeLayout) convertView.findViewById(R.id.root_view);
            holder.imageView = (CircularImageView) convertView.findViewById(R.id.contact_icon);
            holder.displayName = (TextView) convertView.findViewById(R.id.contact_name);
            holder.phoneNumber = (TextView) convertView.findViewById(R.id.contact_phone);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        Contact c = data.get(position);
        holder.displayName.setText(c.getDisplayName());
        holder.imageView.setLetter(c.getDisplayName().charAt(0));
        holder.phoneNumber.setText(c.getPhoneNumber());


        return convertView;
    }

    public void toggleSelection(int position){
        selectView(position, !mSelectedItemsIds.get(position));
    }

    public void selectView(int position, boolean value) {
        if (value)
            mSelectedItemsIds.put(position, value);
        else
            mSelectedItemsIds.delete(position);
        notifyDataSetChanged();
    }

    public void removeSelection() {
        mSelectedItemsIds = new SparseBooleanArray();
        notifyDataSetChanged();
    }

}
