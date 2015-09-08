package com.android.emergencyapp.ui;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import com.android.emergencyapp.R;
import com.android.emergencyapp.util.BusProvider;
import com.android.emergencyapp.util.Log;
import com.android.emergencyapp.util.events.LocationAddressAvailableEvent;

import com.squareup.otto.Subscribe;

/**
 * Created by Nirajan on 8/2/2015.
 */
public class TextFragment extends Fragment {

    private static final String TAG = "TextFragment";

    private TextView title;

    // Floating send button
    private ImageButton sendButton;

    // Track if location address was requested to send automated texts
    private boolean mSendText = false;

    protected final static String KEY_SEND_TEXTS = "send_automated_texts";



    /**
     * @return a new instance of {@link TextFragment}, adding the parameters into a bundle and
     * setting them as arguments.
     */
    public static TextFragment newInstance() {
        TextFragment fragment = new TextFragment();
        return fragment;
    }

    @Override
    public void onResume() {
        super.onResume();
        BusProvider.getInstance().register(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        BusProvider.getInstance().unregister(this);
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(KEY_SEND_TEXTS, mSendText);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Log.d(TAG, "onActivity created");
        if (savedInstanceState != null){
            Log.d(TAG, "Setting boolean send text value " + String.valueOf(savedInstanceState.getBoolean(KEY_SEND_TEXTS)));
            mSendText = savedInstanceState.getBoolean(KEY_SEND_TEXTS, false);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_text, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        title = (TextView) view.findViewById(R.id.item_title);
        sendButton = (ImageButton) view.findViewById(R.id.fab_send);

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // get the street address for current location
                ((MainActivity) getActivity()).fetchCurrentAddress();
            }
        });
    }

    /**
     * Subscribed to the location address available event
     *
     * @param event Location Street address available event
     */
    @Subscribe
    public void onLocationAddressAvailable(LocationAddressAvailableEvent event){
        Log.i(TAG, "Address: " + event.address);
        title.setText(event.address);
    }



}
