package com.android.emergencyapp.ui;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;


import com.android.emergencyapp.R;
import com.android.emergencyapp.provider.ContactDatabaseHelper;
import com.android.emergencyapp.util.AnalyticsManager;
import com.android.emergencyapp.util.BusProvider;
import com.android.emergencyapp.util.Log;
import com.android.emergencyapp.util.PrefUtils;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;


/**
 * Created by Nirajan on 8/1/2015.
 */
public class BaseActivity extends AppCompatActivity{

    private static final String TAG = "BaseActivity";
    private static final int PLAY_SERVICES_RESOLUTION_REQUEST = 9000;

    // Primary toolbar
    private Toolbar mActionBarToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // initialize the Google Analytics Manager
        AnalyticsManager.initializeAnalyticsTracker(getApplicationContext());

        // initialize the Shared Preferences
        PrefUtils.init(this);

        // initialize the DatabaseHelper
        ContactDatabaseHelper.getInstance(this);

        ActionBar ab = getSupportActionBar();
        if(ab != null)
            ab.setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void setContentView(int layoutResId){
        super.setContentView(layoutResId);
        getActionBarToolbar();
    }

    /**
     * Retrieves the toolbar
     * @return Toolbar
     */
    protected Toolbar getActionBarToolbar() {
        if (mActionBarToolbar == null) {
            mActionBarToolbar = (Toolbar) findViewById(R.id.toolbar);
            if (mActionBarToolbar != null) {
                setSupportActionBar(mActionBarToolbar);
            }
        }
        return mActionBarToolbar;
    }

    @Override
    protected void onResume(){
        super.onResume();
        // Recommended to check for Play Services on Activity's resume too
        checkPlayServices();
        // Register Bus provider
        BusProvider.getInstance().register(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        // Always unregister when an object no longer should be on the bus.
        BusProvider.getInstance().unregister(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState){
        super.onPostCreate(savedInstanceState);
        //setupNavDrawer();
        //trySetupSwipeRefresh();
    }

    /**
     * Check the device to make sure it has the Google Play Services APK. If
     * it doesn't, display a dialog that allows users to download the APK from
     * the Google Play Store or enable it in the device's system settings.
     */
    private boolean checkPlayServices(){
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode != ConnectionResult.SUCCESS){
            if (GooglePlayServicesUtil.isUserRecoverableError(resultCode)) {
                GooglePlayServicesUtil.getErrorDialog(resultCode, this,
                        PLAY_SERVICES_RESOLUTION_REQUEST).show();
            } else {
                Log.i(TAG, "This device is not supported.");
                finish();
            }
            return false;
        }
        return true;
    }




}
