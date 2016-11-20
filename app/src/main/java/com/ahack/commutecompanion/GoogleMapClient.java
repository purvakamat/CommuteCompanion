package com.ahack.commutecompanion;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.os.ResultReceiver;

import com.google.android.gms.maps.model.LatLng;

import java.io.IOException;
import java.util.List;

/**
 * Created by Purva Kamat on 11/19/2016.
 */

public class GoogleMapClient extends IntentService {

    ResultReceiver mReceiver;

    public GoogleMapClient() {
        super("GoogleMapClient");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            System.out.println("intent started");
            IntentType intentType = IntentType.fromOrdinal(intent.getIntExtra(Constants.INTENT_TYPE,0));

            switch (intentType) {
                case DESTINATION_LOCATION:
                    mReceiver = intent.getParcelableExtra(Constants.RECEIVER);
                    Geocoder geocoder = new Geocoder(this);
                    String location = intent.getExtras().getString("location");
                    List<Address> addresses;
                    addresses = geocoder.getFromLocationName(location, 1);
                    if(addresses.size() > 0) {
                        double latitude= addresses.get(0).getLatitude();
                        double longitude= addresses.get(0).getLongitude();
                        LatLng dest = new LatLng(latitude, longitude);
                        deliverResultToReceiver(Constants.SUCCESS_RESULT, dest);
                    }
                    break;

                case ROUTES:
                    break;
            }
        } catch (IOException e) {
            deliverResultToReceiver(Constants.FAILURE_RESULT, null);
            e.printStackTrace();
        }
    }

    private void deliverResultToReceiver(int resultCode, LatLng dest) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.RESULT_DATA_KEY, dest);
        mReceiver.send(resultCode, bundle);
    }
}
