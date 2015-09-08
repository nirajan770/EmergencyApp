package com.android.emergencyapp.ui;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.android.emergencyapp.R;
import com.android.emergencyapp.service.LocationAddressIntentService;
import com.android.emergencyapp.util.BusProvider;
import com.android.emergencyapp.util.Constants;
import com.android.emergencyapp.util.Log;
import com.android.emergencyapp.util.events.LocationAddressAvailableEvent;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResult;
import com.google.android.gms.location.LocationSettingsStatusCodes;

import java.text.DateFormat;
import java.util.Date;


public class MainActivity extends BaseActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, LocationListener,
        ResultCallback<LocationSettingsResult> {

    private static final String TAG = "MainActivity";

    // Google Play Services credentials
    private GoogleApiClient mGoogleApiClient;
    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    // Boolean to track whether the app is already resolving an error
    private boolean mResolvingError = false;

    // Represents a geographical location
    private Location mCurrentLocation;
    // Stores parameters for requests to FusedLocationProviderApi
    private LocationRequest mLocationRequest;
    // Used for checking settings to determine if the device has optimal location settings
    private LocationSettingsRequest mLocationSettingsRequest;
    // Boolean to track status of a location update request
    private boolean mRequestingLocationUpdates = false;
    // Stores the time for last location update
    private String mLastUpdateTime = "";

    // Request code for location settings dialog
    private static final int REQUEST_CHECK_SETTINGS = 0x1;
    // Time interval between location updates
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 1000;
    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    public static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    /**
     * Keys for storing Activity state in the Bundle
     */
    protected static final String STATE_RESOLVING_ERROR = "resolving_error";
    protected final static String KEY_REQUESTING_LOCATION_UPDATES = "requesting-location-updates";
    protected final static String KEY_LOCATION = "location";
    protected final static String KEY_LAST_UPDATED_TIME_STRING = "last-updated-time-string";
    protected static final String ADDRESS_REQUESTED_KEY = "address-request-pending";
    protected static final String LOCATION_ADDRESS_KEY = "location-address";

    // Tracks whether street address has been requested by the user
    protected boolean mAddressRequested = false;
    // Stores the known current street address
    protected String mAddressOutput = "";
    // Receiver registered with LocationAddressIntentService
    private StreetAddressReceiver mAddressReceiver;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG, "onCreate");
        setContentView(R.layout.activity_main);

        // instantiate the fragment with sliding tabs
        if (savedInstanceState == null){
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            MainFragment fragment = new MainFragment();
            transaction.replace(R.id.main_content, fragment);
            transaction.commit();
        }

        // register the receiver for street address
        mAddressReceiver = new StreetAddressReceiver(new Handler());

        // update values from the Bundle
        updateValuesFromBundle(savedInstanceState);

        // boolean tracking if app is resolving any Google play services error
        mResolvingError = savedInstanceState != null
                && savedInstanceState.getBoolean(STATE_RESOLVING_ERROR, false);

        // build the GoogleApiClient
        buildGoogleApiClient();

        // set up a location request
        createLocationRequest();

        // set up a location settings request
        createLocationSettingsRequest();
    }

    /**
     * Updates fields with the data stored in the bundle
     */
    private void updateValuesFromBundle(Bundle savedInstanceState){
        if (savedInstanceState != null){
            // Update the value of mRequestingLocationUpdates from the Bundle, and make sure that
            // the Start Updates and Stop Updates buttons are correctly enabled or disabled.
            if (savedInstanceState.keySet().contains(KEY_REQUESTING_LOCATION_UPDATES)) {
                mRequestingLocationUpdates = savedInstanceState.getBoolean(
                        KEY_REQUESTING_LOCATION_UPDATES);
            }

            // Update the value of mCurrentLocation from the Bundle and update the UI to show the
            // correct latitude and longitude.
            if (savedInstanceState.keySet().contains(KEY_LOCATION)) {
                // Since KEY_LOCATION was found in the Bundle, we can be sure that mCurrentLocation
                // is not null.
                mCurrentLocation = savedInstanceState.getParcelable(KEY_LOCATION);
            }

            // Update the value of mLastUpdateTime from the Bundle and update the UI.
            if (savedInstanceState.keySet().contains(KEY_LAST_UPDATED_TIME_STRING)) {
                mLastUpdateTime = savedInstanceState.getString(KEY_LAST_UPDATED_TIME_STRING);
            }

            // Check savedInstanceState to see if the address was previously requested.
            if (savedInstanceState.keySet().contains(ADDRESS_REQUESTED_KEY)) {
                mAddressRequested = savedInstanceState.getBoolean(ADDRESS_REQUESTED_KEY);
            }
            // Check savedInstanceState to see if the location address string was previously found
            // and stored in the Bundle. If it was found, display the address string in the UI.
            if (savedInstanceState.keySet().contains(LOCATION_ADDRESS_KEY)) {
                mAddressOutput = savedInstanceState.getString(LOCATION_ADDRESS_KEY);
            }
        }
    }

    /**
     * Builds a GoogleApiClient. Uses the addApi() method to request the LocationServices API.
     */
    protected synchronized void buildGoogleApiClient() {
        Log.i(TAG, "building GoogleApiClient");
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * Sets up a location request based on the location settings enabled
     */
    private void createLocationRequest(){
        Log.i(TAG, "Location Request created");
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Sets up a location settings request which is used for checking
     * if a device has the needed location settings
     *
     */
    private void createLocationSettingsRequest(){
        Log.i(TAG, "Location Settings Request created");
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
        // check if device's settings are adequate for app's location use
        LocationServices.SettingsApi.checkLocationSettings(mGoogleApiClient,
                mLocationSettingsRequest).setResultCallback(this);
    }

    /**
     * Start a location update request from FusedLocationAPI
     *
     */
    private void startLocationUpdates(){
        Log.i(TAG, "start location updates");
        LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient,
                mLocationRequest,
                this
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                mRequestingLocationUpdates = true;  // set the requesting location update flag to true
            }
        });
    }

    /**
     * Stop location update request from FusedLocationAPI
     *
     */
    private void stopLocationUpdates(){
        Log.i(TAG, "stop location updates");
        LocationServices.FusedLocationApi.removeLocationUpdates(
                mGoogleApiClient,
                this
        ).setResultCallback(new ResultCallback<Status>() {
            @Override
            public void onResult(Status status) {
                mRequestingLocationUpdates = false;  // set the requesting location update flag to false
            }
        });
    }

    @Override
    protected void onSaveInstanceState(Bundle savedInstanceState) {
        savedInstanceState.putBoolean(STATE_RESOLVING_ERROR, mResolvingError);
        savedInstanceState.putBoolean(KEY_REQUESTING_LOCATION_UPDATES, mRequestingLocationUpdates);
        savedInstanceState.putParcelable(KEY_LOCATION, mCurrentLocation);
        savedInstanceState.putString(KEY_LAST_UPDATED_TIME_STRING, mLastUpdateTime);
        // Save whether the address has been requested.
        savedInstanceState.putBoolean(ADDRESS_REQUESTED_KEY, mAddressRequested);
        // Save the address string.
        savedInstanceState.putString(LOCATION_ADDRESS_KEY, mAddressOutput);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    protected void onStart(){
        super.onStart();
        // start connection only if no error is being resolved
        if(!mResolvingError){
            Log.i(TAG, "onStart(), connecting GoogleApiClient");
            mGoogleApiClient.connect();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        // Within {@code onPause()}, we pause location updates, but leave the
        // connection to GoogleApiClient intact.  Here, we resume receiving
        // location updates if the user has requested them.
        if (mGoogleApiClient.isConnected() && mRequestingLocationUpdates) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause(){
        super.onPause();
        if(mGoogleApiClient.isConnected()){
            // stop the location updates
            stopLocationUpdates();
        }
    }

    @Override
    protected void onStop() {
        if (mGoogleApiClient.isConnected()) {
            Log.i(TAG, "onStop(), disconnecting GoogleApiClient");
            mGoogleApiClient.disconnect();
        }
        super.onStop();
    }

    /**
     * Triggers the address request of the current location
     *
     */
    public void fetchCurrentAddress(){
        Log.i(TAG, "fetchCurrentAddress called");
        if (mGoogleApiClient.isConnected() && mCurrentLocation != null){
            //getStreetAddress();
            startIntentService();
        }
        mAddressRequested = true;
    }

    /**
     * Creates an intent, adds location data to it as an extra, and starts the intent service for
     * fetching an address.
     */
    protected void startIntentService() {
        // Create an intent for passing to the intent service responsible for fetching the address.
        Intent intent = new Intent(this, LocationAddressIntentService.class);

        // Pass the result receiver as an extra to the service.
        intent.putExtra(Constants.RECEIVER, mAddressReceiver);

        // Pass the location data as an extra to the service.
        intent.putExtra(Constants.LOCATION_DATA_EXTRA, mCurrentLocation);

        // Start the service. If the service isn't already running, it is instantiated and started
        // (creating a process for it if needed); if it is running then it remains running. The
        // service kills itself automatically once all intents are processed.
        startService(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    /**
     * Implementation of LocationListener interface
     *
     * Invoked when the location changes
     * @param location
     */
    @Override
    public void onLocationChanged(Location location) {
        Log.i(TAG, "Location changed interface");
        // update the current location
        mCurrentLocation = location;
        Log.d(TAG, "New Location: " + mCurrentLocation.toString());
        // update the last location time update
        mLastUpdateTime = DateFormat.getTimeInstance().format(new Date());
    }

    /**
     * Implementation of LocationSettingsResult interface
     *
     * Invoked when LocationSettingsRequest is called     *
     * @param locationSettingsResult
     * locationSettingsResult determines if the settings are adequate
     */

    @Override
    public void onResult(LocationSettingsResult locationSettingsResult) {
        Log.i(TAG, "LocationSettingsResult onResult");
        final Status status = locationSettingsResult.getStatus();
        switch(status.getStatusCode()){
            case LocationSettingsStatusCodes.SUCCESS:
                Log.i(TAG, "Location Settings Satisfied");
                // start the location updates
                startLocationUpdates();
                break;
            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                Log.i(TAG, "Location Settings NOT satisfied. Display dialog box to prompt action");
                try {
                    status.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                } catch (IntentSender.SendIntentException e){
                    e.printStackTrace();
                    Log.i(TAG, "PendingIntent unable to execute resolution request");
                }
            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                Log.i(TAG, "Location settings are inadequate, and cannot be fixed here. Dialog " +
                        "not created.");
                break;
        }
    }

    /**
     * Implementation of ConnectionCallbacks interface
     *
     * @param bundle
     */
    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "GoogleApiClient successfully connected");

        // Retrieve the last known location
        mCurrentLocation = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
        //Log.i(TAG, "CURRENT LOCATION: " + mCurrentLocation.toString());
        if(mCurrentLocation != null) {
            Log.d(TAG, "Latitude: " + mCurrentLocation.getLatitude());
            Log.d(TAG, "Longitude: " + mCurrentLocation.getLongitude());
            // Determine whether a Geocoder is available.
            if (!Geocoder.isPresent()) {
                Toast.makeText(this, "No Geocoder available", Toast.LENGTH_LONG).show();
                return;
            }
        }

        if (mAddressRequested)
            startIntentService();
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.e(TAG, "GoogleApiClient connection suspended");
        // Retry connecting
        mGoogleApiClient.connect();
    }

    /**
     * Implementation of ConnectionFailed interface
     * @param connectionResult
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (mResolvingError){
            // Already attempting to resolve an error
            Log.i(TAG, "Already attempting to resolve Google Play Services connection error");
        } else if (connectionResult.hasResolution()){
            try{
                mResolvingError = true;
                connectionResult.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
            }catch (IntentSender.SendIntentException e){
                // Error with resolution intent, try again
                mGoogleApiClient.connect();
            }
        } else {
            // Show dialog using GooglePlayServicesUtil
            showErrorDialog(connectionResult.getErrorCode());
            mResolvingError = true;
        }
    }

    /* Creates a dialog for an error message */
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "errordialog");
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        mResolvingError = false;
    }

    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() { }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GooglePlayServicesUtil.getErrorDialog(errorCode,
                    this.getActivity(), REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((MainActivity)getActivity()).onDialogDismissed();
        }
    }

    /**
     * OnActivityResult received from startResolutionForResult() or
     * GooglePlayServicesUtil.getErrorDialog()
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        if (requestCode == REQUEST_RESOLVE_ERROR){
            mResolvingError = false; // set the error resolving flag to false
            if (resultCode == RESULT_OK){
                // start connection, if GoogleApiClient not connected
                if (!mGoogleApiClient.isConnecting() &&
                        !mGoogleApiClient.isConnected()){
                    mGoogleApiClient.connect();
                }
            }
        }

        if (requestCode == REQUEST_CHECK_SETTINGS){
            if (resultCode == RESULT_OK){
                Log.i(TAG, "User agreed to make required location settings changes.");
                // start location updates
                startLocationUpdates();
            }
            if (resultCode == RESULT_CANCELED){
                Log.i(TAG, "User cancelled request to update location settings");
                // Display info that app may not perform correctly without location
            }
        }else{
            Log.d(TAG, "super.onActivityResult called, Request Code: " + String.valueOf(requestCode));
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Receiver for data sent from FetchAddressIntentService.
     */
    class StreetAddressReceiver extends ResultReceiver {
        public StreetAddressReceiver(Handler handler) {
            super(handler);
        }

        /**
         *  Receives data sent from LocationAddressIntentService.
         */
        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {

            // Display the address string or an error message sent from the intent service.
            mAddressOutput = resultData.getString(Constants.RESULT_DATA_KEY);

            // Post event based on success or failure to retrieve the street address
            if (resultCode == Constants.SUCCESS_RESULT) {
                Log.d(TAG, "ADDRESS RETRIEVED: " + mAddressOutput);
                BusProvider.getInstance().post(new LocationAddressAvailableEvent(Constants.SUCCESS_RESULT, mAddressOutput));

            } else if (resultCode == Constants.FAILURE_RESULT){
                Log.d(TAG, "ERROR MESSAGE: " + mAddressOutput);
                BusProvider.getInstance().post(new LocationAddressAvailableEvent(Constants.FAILURE_RESULT, mAddressOutput));
            }

            // Reset the address requested boolean flag
            mAddressRequested = false;

        }
    }
}
