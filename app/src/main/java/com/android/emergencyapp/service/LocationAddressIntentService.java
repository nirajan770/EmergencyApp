package com.android.emergencyapp.service;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;

import com.android.emergencyapp.R;
import com.android.emergencyapp.util.BusProvider;
import com.android.emergencyapp.util.Constants;
import com.android.emergencyapp.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 */
public class LocationAddressIntentService extends IntentService {

    private static final String TAG = "LocationAddressIntentService";

    /**
     * The receiver where results are forwarded from this service.
     */
    protected ResultReceiver mReceiver;

    public LocationAddressIntentService() {
        super(TAG);

    }

    @Override
    protected void onHandleIntent(Intent intent) {
        String errorMessage = "";

        if (intent != null) {

            mReceiver = intent.getParcelableExtra(Constants.RECEIVER);

            // Check if receiver was assigned
            if (mReceiver == null){
                Log.e(TAG, "No receiver assigned to the service");
                return;
            }


            // Retrieve location passed to the service
            Location location = intent.getParcelableExtra(Constants.LOCATION_DATA_EXTRA);

            if (location == null){
                errorMessage = "No location provided to IntentService";
                Log.e(TAG, errorMessage);
                deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
                return;
            }

            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            // List of addresses found
            List<Address> addressList = null;
            try{
                addressList = geocoder.getFromLocation(
                        location.getLatitude(),
                        location.getLongitude(),
                        1);
            } catch (IOException i){
                i.printStackTrace();
                errorMessage = getString(R.string.service_not_available);
                Log.e(TAG, errorMessage);
                deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
            } catch (IllegalArgumentException a){
                a.printStackTrace();
                // Catch invalid latitude or longitude values.
                errorMessage = getString(R.string.invalid_lat_long_used);
                Log.e(TAG, errorMessage + ". " +
                        "Latitude = " + location.getLatitude() +
                        ", Longitude = " + location.getLongitude());
                deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
            }

            if (addressList == null || addressList.size() == 0){
                Log.e(TAG, "No Address Found");
                if (errorMessage.isEmpty()) {
                    errorMessage = getString(R.string.no_address_found);
                    Log.e(TAG, errorMessage);
                }
                deliverResultToReceiver(Constants.FAILURE_RESULT, errorMessage);
            } else {
                Address address = addressList.get(0);
                ArrayList<String> addressFragments = new ArrayList<String>();
                for(int i = 0; i < address.getMaxAddressLineIndex(); i++) {
                    addressFragments.add(address.getAddressLine(i));
                }
                Log.i(TAG, getString(R.string.address_found));
                deliverResultToReceiver(Constants.SUCCESS_RESULT,
                        TextUtils.join(System.getProperty("line.separator"), addressFragments));
            }

        }
    }


    /**
     * Sends a resultCode and message to the receiver.
     */
    private void deliverResultToReceiver(int resultCode, String message) {
        Bundle bundle = new Bundle();
        bundle.putString(Constants.RESULT_DATA_KEY, message);
        mReceiver.send(resultCode, bundle);
    }

}
