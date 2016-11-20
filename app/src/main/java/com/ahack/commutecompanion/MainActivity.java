package com.ahack.commutecompanion;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.Handler;
import android.support.v4.app.ActivityCompat;
import android.support.v4.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, EditText.OnEditorActionListener {

    private GoogleMap mMap;
    private Context context;
    private AddressResultReceiver mResultReceiver;
    private static final int PERMISSION_REQUEST_CODE = 1;
    private List<LatLng> mMarkers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        context = getApplicationContext();
        EditText dest_text = (EditText) this.findViewById(R.id.dest_text);
        dest_text.setOnEditorActionListener(this);
        mResultReceiver = new AddressResultReceiver(new Handler());
        mMarkers = new ArrayList<LatLng>();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        if(!checkPermission())
            requestPermission();

        setupMyLocation();
    }

    private boolean checkPermission() {
        int result = ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION);
        if (result == PackageManager.PERMISSION_GRANTED)
            return true;
        else
            return false;
    }

    private void requestPermission() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.ACCESS_FINE_LOCATION)) {

            Toast.makeText(context, "GPS permission allows us to access location data. Please allow in App Settings for additional functionality.", Toast.LENGTH_LONG).show();

        } else {

            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupMyLocation();
                }
                break;
        }
    }

    private void setupMyLocation(){
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            mMap.setMyLocationEnabled(true);
            mMap.getUiSettings().setMyLocationButtonEnabled(true);

            // Getting LocationManager object from System Service LOCATION_SERVICE
            LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

            // Creating a criteria object to retrieve provider
            Criteria criteria = new Criteria();

            // Getting the name of the best provider
            String provider = locationManager.getBestProvider(criteria, true);

            // Getting Current Location
            Location location = locationManager.getLastKnownLocation(provider);

            if (location != null) {
                // Getting latitude of the current location
                double latitude = location.getLatitude();
                // Getting longitude of the current location
                double longitude = location.getLongitude();
                LatLng myPosition = new LatLng(latitude, longitude);
                mMarkers.add(myPosition);

                mMap.addMarker(new MarkerOptions().position(myPosition).title("Start"));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(myPosition));
            }
        }
    }

    @Override
    public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {
        if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                actionId == EditorInfo.IME_ACTION_DONE ||
                keyEvent.getAction() == KeyEvent.ACTION_DOWN &&
                        keyEvent.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
            if (!keyEvent.isShiftPressed()) {
                // the user is done typing, search for the location.
                EditText dest = (EditText) textView;
                Intent i = new Intent(this, GoogleMapClient.class);
                i.putExtra(Constants.INTENT_TYPE,IntentType.DESTINATION_LOCATION);
                i.putExtra(Constants.RECEIVER, mResultReceiver);
                i.putExtra("location", dest.getText().toString());
                startService(i);
                return true; // consume
            }
        }
        return false; // pass on to other listeners.
    }

    private void adjustMap(){
        LatLngBounds.Builder b = new LatLngBounds.Builder();
        for (LatLng m : mMarkers) {
            b.include(m);
        }
        LatLngBounds bounds = b.build();
        mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(bounds,100));
    }

    private void plotPaths(){

    }

    class AddressResultReceiver extends ResultReceiver {

        public AddressResultReceiver(Handler handler) {
            super(handler);
        }

        @Override
        protected void onReceiveResult(int resultCode, Bundle resultData) {
            LatLng location = resultData.getParcelable(Constants.RESULT_DATA_KEY);

            if (resultCode == Constants.SUCCESS_RESULT) {
                if (location != null) {
                    mMap.addMarker(new MarkerOptions().position(location).title("Destination"));
                    mMarkers.add(location);
                    adjustMap();
                }
            }
        }
    }
}

