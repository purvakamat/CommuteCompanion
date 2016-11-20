package com.ahack.commutecompanion;

import android.app.IntentService;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.Parcelable;
import android.provider.Settings;
import android.support.v4.os.ResultReceiver;

import com.ahack.commutecompanion.GMapResponse.Leg;
import com.ahack.commutecompanion.GMapResponse.ParseResponse;
import com.ahack.commutecompanion.GMapResponse.Route;
import com.ahack.commutecompanion.GMapResponse.Step;
import com.google.android.gms.maps.model.LatLng;

import org.w3c.dom.Document;

import java.io.IOException;
import java.util.ArrayList;
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
            int iType = intent.getIntExtra(Constants.INTENT_TYPE,1);
            IntentType intentType = IntentType.fromOrdinal(iType);
            mReceiver = intent.getParcelableExtra(Constants.RECEIVER);

            switch (intentType) {
                case DESTINATION_LOCATION:
                    Geocoder geocoder = new Geocoder(this);
                    String location = intent.getExtras().getString(Constants.LOCATION);
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
                    LatLng startLoc = intent.getParcelableExtra(Constants.START_LOCATION);
                    LatLng destLoc = intent.getParcelableExtra(Constants.DEST_LOCATION);
                    ParseResponse response = new ParseResponse();
                    String jsonResponse = response.convertStreamToString(response.fetch(startLoc, destLoc,ParseResponse.MODE_WALKING));
                    System.out.println(jsonResponse);
                    List<Route> directionPoint =  response.parse(jsonResponse);
                    List<ArrayList<LatLng>> routes = new ArrayList<ArrayList<LatLng>>();
                    for(Route r: directionPoint){
                        ArrayList<LatLng> route = new ArrayList<LatLng>();
                        for(Leg l:r.getLegs()){
                            for(Step s: l.getSteps()){
                                route.addAll(s.getPoints());
                            }
                        }
                        routes.add(route);
                    }
                    deliverResultToReceiver(Constants.SUCCESS_RESULT, routes);
                    break;
            }
        } catch (IOException e) {
            //deliverResultToReceiver(Constants.FAILURE_RESULT, null);
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deliverResultToReceiver(int resultCode, LatLng dest) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(Constants.RESULT_DATA_KEY, dest);
        mReceiver.send(resultCode, bundle);
    }

    private void deliverResultToReceiver(int resultCode, List<ArrayList<LatLng>> routes) {
        Bundle bundle = new Bundle();
        bundle.putInt(Constants.PATH_COUNT, routes.size());
        for(int i = 0; i<routes.size(); i++){
            bundle.putParcelableArrayList(String.valueOf(i), routes.get(i));
        }
        mReceiver.send(resultCode, bundle);
    }
}
