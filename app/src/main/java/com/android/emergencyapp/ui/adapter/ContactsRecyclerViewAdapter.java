/*
package com.android.emergencyapp.ui.adapter;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.emergencyapp.R;
import com.android.emergencyapp.model.Contact;
import com.android.emergencyapp.ui.widget.CircularImageView;
import com.android.emergencyapp.util.Log;

import java.util.ArrayList;
import java.util.List;

*/
/**
 * Created by Nirajan on 8/15/2015.
 *//*

public class ContactsRecyclerViewAdapter extends RecyclerView.Adapter<ContactsRecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "ContactsRecyclerViewAdapter";

    private List<Contact> data;
    private Context mContext;

    // Click listener
    onItemClickListener mItemClickListener;

    public interface onItemClickListener{
        public void onItemClick(View view, int position);
        public void onItemLongClick(View view, int position);
    }

    public void setOnItemClickListener(final onItemClickListener mItemClickListener){
        this.mItemClickListener = mItemClickListener;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
                                            View.OnLongClickListener{
        public RelativeLayout rootView;
        public CircularImageView imageView;
        public TextView displayName;
        public TextView phoneNumber;

        public ViewHolder(View v) {
            super(v);
            this.rootView = (RelativeLayout) v.findViewById(R.id.root_view);
            this.imageView = (CircularImageView) v.findViewById(R.id.contact_icon);
            this.displayName = (TextView) v.findViewById(R.id.contact_name);
            this.phoneNumber = (TextView) v.findViewById(R.id.contact_phone);
            v.setOnClickListener(this);
            v.setOnLongClickListener(this);
        }

        @Override
        public void onClick(View v) {
            Log.d(TAG, "item on click");
            if (mItemClickListener != null)
                mItemClickListener.onItemClick(v, getAdapterPosition());
        }

        @Override
        public boolean onLongClick(View v) {
            Log.d(TAG, "item on long click");
            if (mItemClickListener != null){
                Log.d(TAG, "long click passed to interface implementation");
                mItemClickListener.onItemLongClick(v, getAdapterPosition());
            }

            return true;
        }
    }



    public ContactsRecyclerViewAdapter(Context context, List<Contact> data) {
        this.mContext = context;
        this.data = data;
    }

    @Override
    public ContactsRecyclerViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                                .inflate(R.layout.contacts_list, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Contact c = data.get(position);
        holder.displayName.setText(c.getDisplayName());
        holder.imageView.setLetter(c.getDisplayName().charAt(0));
        holder.phoneNumber.setText(c.getPhoneNumber());
        if (c.isSelected()){
            holder.rootView.setBackgroundColor(Color.parseColor("#F5F5F5"));
        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public List<Contact> getList(){
        return data;
    }

    public Contact getItem(int position){
        return data.get(position);
    }

    public void addItem(Contact contact){
        data.add(contact);
    }

    public void removeItem(int position){
        data.remove(position);
    }

}
*/
