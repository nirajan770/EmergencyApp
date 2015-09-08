package com.android.emergencyapp.util.events;

/**
 * Created by Nirajan on 8/2/2015.
 */
public class LocationAddressAvailableEvent {
    // to denote success or failure in getting address of the current location
    public final int result;
    public final String address;

    public LocationAddressAvailableEvent(int result, String address){
        this.result = result;
        this.address = address;
    }
}
