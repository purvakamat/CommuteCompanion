package com.ahack.commutecompanion.CrimeData;


import com.ahack.commutecompanion.GMapResponse.Keys;
import com.ahack.commutecompanion.GMapResponse.Route;
import com.google.android.gms.maps.model.LatLng;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class CrimeData_Parser {
    public final String crime_file = "";

    public List<LatLng> getParsedCrimeData(){
        String fileContent = "";
        final JSONObject jSONObject;
        try {
            jSONObject = new JSONObject(fileContent);
            JSONArray routeJSONArray = jSONObject.getJSONArray(Keys.ROUTES);

            Route route;
            JSONObject routesJSONObject;
            for (int m = 0; m < routeJSONArray.length(); m++) {
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        List<LatLng> crime_data = new ArrayList<LatLng>();
        return crime_data;
    }
}
