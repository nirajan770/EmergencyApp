package com.android.emergencyapp.util;

import android.content.Context;
import android.util.Log;

import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by Nirajan on 8/1/2015.
 */
public class AnalyticsManager {
    private static final String TAG = "AnalyticsManager";

    private static Context sAppContext = null;

    /**
     * Google Analytics Property ID for the application
     */
    private static final String PROPERTY_ID = "UA-******-*";

    /**
     * The Analytics singleton. The field is set in onCreate method override when the application
     * class is initially created.
     */
    private static GoogleAnalytics analytics;

    /**
     * The default app tracker. The field is from onCreate callback when the application is
     * initially created.
     */
    private static Tracker mTracker;

    public static synchronized void setTracker(Tracker tracker){
        mTracker = tracker;
    }

    /**
     * Sends a screen view of the current display screen
     */
    public static void sendScreenView(String screenName){
        mTracker.setScreenName(screenName);
        mTracker.send(new HitBuilders.AppViewBuilder().build());
        Log.d(TAG, "Screen View Recorded");
    }

    /**
     * Sends an event to the analytics
     * @param category
     * @param action
     * @param label
     * @param value
     */
    public static void sendEvent(String category, String action, String label, long value){
        mTracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label)
                .setValue(value)
                .build());
        Log.d(TAG, "Event recorded:");
        Log.d(TAG, "\tCategory: " + category);
        Log.d(TAG, "\tAction: " + action);
        Log.d(TAG, "\tLabel: " + label);
        Log.d(TAG, "\tValue: " + value);
    }

    /**
     * Send event without a value
     * @param category
     * @param action
     * @param label
     */
    public static void sendEvent(String category, String action, String label) {
        sendEvent(category, action, label, 0);
    }

    public static void sendCaughtException(String description, boolean isFatal){
        mTracker.send(new HitBuilders.ExceptionBuilder()
                .setDescription(description)
                .setFatal(isFatal)
                .build());
        Log.d(TAG, "Exception Recorded");
        Log.d(TAG, "\tDescription: " + description);
        Log.d(TAG, "\tisFatal: " + String.valueOf(isFatal));
    }


    public Tracker getTracker() {
        return mTracker;
    }

    /**
     * Access to the global Analytics singleton. If this method returns null you forgot to either
     * set android:name="&lt;this.class.name&gt;" attribute on your application element in
     * AndroidManifest.xml or you are not setting this.analytics field in onCreate method override.
     */
    public static GoogleAnalytics analytics() {
        return analytics;
    }

    /**
     * Initialize the Analytics Tracker
     */
    public static synchronized void initializeAnalyticsTracker(Context context){
        sAppContext = context;
        if (mTracker == null){
            analytics = GoogleAnalytics.getInstance(context);

            mTracker = analytics.newTracker(PROPERTY_ID);

            // Provide unhandled exceptions reports. Do that first after creating the tracker
            mTracker.enableExceptionReporting(true);

            // Enable Re-marketing, Demographics & Interests reports
            // https://developers.google.com/analytics/devguides/collection/android/display-features
            mTracker.enableAdvertisingIdCollection(true);

            // Enable automatic activity tracking for your app
            mTracker.enableAutoActivityTracking(true);

        }
    }
}
