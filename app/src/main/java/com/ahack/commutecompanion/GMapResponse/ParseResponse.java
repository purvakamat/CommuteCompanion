package com.ahack.commutecompanion.GMapResponse;


import android.provider.Settings;
import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class ParseResponse {

    public final static String MODE_DRIVING = "driving";
    public final static String MODE_WALKING = "walking";

    public InputStream fetch(LatLng start, LatLng end, String mode) {

        String url = "http://maps.googleapis.com/maps/api/directions/json?"
                + "origin=" + start.latitude + "," + start.longitude
                + "&destination=" + end.latitude + "," + end.longitude
                + "&alternatives=true";

        Log.d("url", url);

        HttpURLConnection urlConnection = null;

        try {
            urlConnection = (HttpURLConnection) new URL(url).openConnection();
            InputStream in = new BufferedInputStream(urlConnection.getInputStream());
            return in;
        }
        catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if(urlConnection != null)
                urlConnection.disconnect();
        }
        return null;
    }

    public String convertStreamToString(final InputStream input) throws Exception {
        try {
            final BufferedReader reader = new BufferedReader(new InputStreamReader(input));
            final StringBuffer sBuf = new StringBuffer();
            String line = null;
            while ((line = reader.readLine()) != null) {
                sBuf.append(line);
            }
            return sBuf.toString();
        } catch (Exception e) {
            throw e;
        } finally {
            try {
                input.close();
            } catch (Exception e) {
                throw e;
            }
        }
    }

    public List<Route> parse(String routesJSONString) throws Exception {
        try {
            List<Route> routeList = new ArrayList<Route>();
            final JSONObject jSONObject = new JSONObject(routesJSONString);
            JSONArray routeJSONArray = jSONObject.getJSONArray(Keys.ROUTES);
            Route route;
            JSONObject routesJSONObject;
            for (int m = 0; m < routeJSONArray.length(); m++) {
                route = new Route();
                routesJSONObject = routeJSONArray.getJSONObject(m);
                JSONArray legsJSONArray;
                route.setSummary(routesJSONObject.getString(Keys.SUMMARY));
                legsJSONArray = routesJSONObject.getJSONArray(Keys.LEGS);
                JSONObject legJSONObject;
                Leg leg;
                JSONArray stepsJSONArray;
                for (int b = 0; b < legsJSONArray.length(); b++) {
                    leg = new Leg();
                    legJSONObject = legsJSONArray.getJSONObject(b);
                    leg.setDistance(new Distance(legJSONObject.optJSONObject(Keys.DISTANCE).optString(Keys.TEXT), legJSONObject.optJSONObject(Keys.DISTANCE).optLong(Keys.VALUE)));
                    leg.setDuration(new Duration(legJSONObject.optJSONObject(Keys.DURATION).optString(Keys.TEXT), legJSONObject.optJSONObject(Keys.DURATION).optLong(Keys.VALUE)));
                    stepsJSONArray = legJSONObject.getJSONArray(Keys.STEPS);
                    JSONObject stepJSONObject, stepDurationJSONObject, legPolyLineJSONObject, stepStartLocationJSONObject, stepEndLocationJSONObject;
                    Step step;
                    String encodedString;
                    LatLng stepStartLocationLatLng, stepEndLocationLatLng;
                    for (int i = 0; i < stepsJSONArray.length(); i++) {
                        stepJSONObject = stepsJSONArray.getJSONObject(i);
                        step = new Step();
                        JSONObject stepDistanceJSONObject = stepJSONObject.getJSONObject(Keys.DISTANCE);
                        step.setDistance(new Distance(stepDistanceJSONObject.getString(Keys.TEXT), stepDistanceJSONObject.getLong(Keys.VALUE)));
                        stepDurationJSONObject = stepJSONObject.getJSONObject(Keys.DURATION);
                        step.setDuration(new Duration(stepDurationJSONObject.getString(Keys.TEXT), stepDurationJSONObject.getLong(Keys.VALUE)));
                        stepEndLocationJSONObject = stepJSONObject.getJSONObject(Keys.END_LOCATION);
                        stepEndLocationLatLng = new LatLng(stepEndLocationJSONObject.getDouble(Keys.LATITUDE), stepEndLocationJSONObject.getDouble(Keys.LONGITUDE));
                        step.setEndLocation(stepEndLocationLatLng);
                        step.setHtmlInstructions(stepJSONObject.getString(Keys.HTML_INSTRUCTION));
                        legPolyLineJSONObject = stepJSONObject.getJSONObject(Keys.POLYLINE);
                        encodedString = legPolyLineJSONObject.getString(Keys.POINTS);
                        step.setPoints(decodePolyLines(encodedString));
                        stepStartLocationJSONObject = stepJSONObject.getJSONObject(Keys.START_LOCATION);
                        stepStartLocationLatLng = new LatLng(stepStartLocationJSONObject.getDouble(Keys.LATITUDE), stepStartLocationJSONObject.getDouble(Keys.LONGITUDE));
                        step.setStartLocation(stepStartLocationLatLng);
                        leg.addStep(step);
                    }
                    route.addLeg(leg);
                }
                routeList.add(route);
            }
            return routeList;
        } catch (Exception e) {
            throw e;
        }
    }

    private ArrayList<LatLng> decodePolyLines(String encoded) {
        ArrayList<LatLng> poly = new ArrayList<LatLng>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;
        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;
            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng position = new LatLng((double) lat / 1E5, (double) lng / 1E5);
            poly.add(position);
        }
        return poly;
    }
}
