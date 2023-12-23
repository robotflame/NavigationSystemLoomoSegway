package com.example.testapp_3_5_1;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URL;
import java.net.HttpURLConnection;
import java.util.Base64;

// Class for receiving and extracting loomos current position
class Cisco {

    static class getLoomoCoordinates extends AsyncTask<Void, Void, JSONObject> {

        protected JSONObject doInBackground(Void... Params){

            String webPage = "https://cmx.uia.no/api/location/v3/clients?macAddress=f4:4d:30:c3:18:fd";
            JSONObject coordinates = new JSONObject();

            try{
                URL url = new URL(webPage);

                String authenticator = "loomo:Y&qlgebubsdv0qtiqfr";
                String encodedAuth = null;
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    encodedAuth = Base64.getEncoder().encodeToString(authenticator.getBytes());
                }

                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setRequestProperty("Authorization", "Basic " + encodedAuth);

                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    try{
                        BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
                        StringBuilder responseBuilder = new StringBuilder();
                        String responseLine = null;
                        while ((responseLine = reader.readLine()) != null) {
                            responseBuilder.append(responseLine.trim());
                        }

                        String jsonResponse = responseBuilder.toString();
                        JSONArray jsonArray = new JSONArray(jsonResponse);
                        JSONObject result = jsonArray.getJSONObject(0);

                        // Locating down to where longitude, latitude and floor are located
                        JSONObject geoCoordinates = result.getJSONObject("geoCoordinate");
                        JSONObject hierarchyDetails = result.getJSONObject("hierarchyDetails");
                        JSONObject floorInfo = hierarchyDetails.getJSONObject("floor");

                        // Extracting longitude, latitude and floor into separate variables
                        double longitude = geoCoordinates.getDouble("longitude");
                        double latitude = geoCoordinates.getDouble("latitude");
                        int floor = floorInfo.getInt("name");

                        // Putting the variables into the JSON Object as return value
                        coordinates.put("longitude", longitude);
                        coordinates.put("latitude", latitude);
                        coordinates.put("floor", floor);

                    }
                    catch(Exception e){
                        System.out.println("Device offline or outside range... Please re-check");
                    }

                } else {
                    System.out.println("HTTP error code: " + responseCode);
                }

                return coordinates;
            } catch(Exception ignored){}

            return null;
        }
    }
}
