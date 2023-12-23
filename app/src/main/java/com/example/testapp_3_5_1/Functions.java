package com.example.testapp_3_5_1;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

class Functions {

    // Printing out where loomo needs to go, what direction to face and degrees to turn itself
    // Used for debugging purposes
    static void movementInstruction(JSONArray coords, JSONArray angles, JSONArray loomoAngle) throws JSONException{

        for (int i = 0; i < coords.length(); i++){
            JSONArray coordinates = coords.getJSONArray(i);
            JSONArray start = coordinates.getJSONArray(0);
            JSONArray destination = coordinates.getJSONArray(1);

            double startLong = start.getDouble(0);
            double startLat = start.getDouble(1);
            double destLong = destination.getDouble(0);
            double destLat = destination.getDouble(1);

            if (i == 0){
                System.out.print(String.format("You are starting at (%s, %s). ", startLong, startLat));
                System.out.print("Face " + angles.get(i) + " radians and move to ");
                System.out.println(String.format("(%s, %s)", destLong, destLat));
            }
            else{
                try{
                    double angle = angles.getDouble(i);
                    double loomoTurn = loomoAngle.getDouble(i-1);
                    System.out.print("Turn loomo " + loomoTurn + " radians. ");
                    System.out.print("Face " + angle + " radians. ");
                } catch (Exception ignored){}
                System.out.println("Move to (" + destLong + ", " + destLat + ")");
            }
        }
    }

    // Returning the angles the loomo has to turn from its current position to the next
    static JSONArray loomoTurnAngles(JSONArray angles){

        JSONArray loomoAngles = new JSONArray();

        for (int i = 0; i < angles.length(); i++){
            try{
                double angle1 = angles.getDouble(i);
                double angle2 = angles.getDouble(i+1);

                double loomoTurn = angle2 - angle1;
                // Need to have negative multiplication, as loomo turn from the unit circle would be opposite
                loomoTurn = loomoTurn *-1;
                loomoAngles.put(loomoTurn);

            } catch(Exception ignored){}
        }

        return loomoAngles;
    }

    // Bearing angle between current position and the next
    static JSONArray geoAngle(JSONArray coordsArray) throws JSONException{

        JSONArray geoAngles = new JSONArray();

        for (int i = 0; i < coordsArray.length(); i++){
            JSONArray coords = coordsArray.getJSONArray(i);
            JSONArray coord1 = coords.getJSONArray(0);
            JSONArray coord2 = coords.getJSONArray(1);

            double lon1 = coord1.getDouble(0);
            double lon2 = coord2.getDouble(0);
            double lat1 = coord1.getDouble(1);
            double lat2 = coord2.getDouble(1);

            // Converting to radians, as this is needed for calculating bearing angle
            double radLat1 = lat1 * (Math.PI / 180);
            double radLat2 = lat2 * (Math.PI / 180);
            double radLon1 = lon1 * (Math.PI / 180) * Math.cos(radLat1);
            double radLon2 = lon2 * (Math.PI / 180) * Math.cos(radLat2);

            double X = Math.sin(radLon2-radLon1)*Math.cos(radLat2);
            double Y = Math.cos(radLat1)*Math.sin(radLat2) - Math.sin(radLat1)*Math.cos(radLat2)*Math.cos(radLon2-radLon1);

            // Angle given in radians.
            double bearing = Math.atan2(X, Y);

            geoAngles.put(bearing);
        }

        return geoAngles;

    }

    // Looping through all coordinate points through the "path" description
    static JSONArray pathCoordinates(JSONArray path) throws JSONException{

        JSONArray coordinates = new JSONArray();

        JSONArray jsonArray = new JSONArray(path.toString());
        JSONObject paths = jsonArray.getJSONObject(0).getJSONObject("path");
        JSONArray features = paths.getJSONArray("features");

        for (int i = 0; i < features.length(); i++){
            JSONObject feature = features.getJSONObject(i);
            JSONObject geometry = feature.getJSONObject("geometry");
            if (geometry.getString("type").equals("LineString")){
                JSONArray coords = geometry.getJSONArray("coordinates");
                coordinates.put(coords);
            }
        }

        return coordinates;
    }


    // Retrieving the distance given in meters from current point to the next point
    static JSONArray getDistance(JSONArray path) throws JSONException {

        JSONArray distance = new JSONArray();

        JSONArray jsonArray = new JSONArray(path.toString());
        JSONObject paths = jsonArray.getJSONObject(0).getJSONObject("path");
        JSONArray features = paths.getJSONArray("features");

        for (int i = 0; i < features.length(); i++){
            JSONObject feature = features.getJSONObject(i);
            JSONObject properties = feature.getJSONObject("properties");
            double meters = properties.getDouble("m");
            distance.put(meters);
        }

        return distance;
    }

    // Boolean returned as the given room exists or not
    static boolean checkRoomAvailable(String destinationRoom, InputStream inputStream){
        boolean roomExists = false;

        try {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            String jsonString = stringBuilder.toString();
            JSONArray jsonArray = new JSONArray(jsonString);
            for (int i = 0; i < jsonArray.length(); i++) {
                if (jsonArray.get(i) instanceof JSONObject) {
                    JSONObject tempObject = jsonArray.getJSONObject(i);

                    // If the room with the given name exists, setting the return object equal the given room
                    if (tempObject.has("name") && tempObject.getString("name").equals(destinationRoom)) {
                        roomExists = true;
                        break;
                    }
                }
            }
        } catch (JSONException | IOException e){
            e.printStackTrace();
        }

        return roomExists;
    }

    // Returning latitude as meters (float)
    static float latitudeToMeters(double startLat, double destLat){

        float latitude;

        latitude = (float)((destLat - startLat) * 111320);
        return latitude;
    }

    // Returning longitude as meters (float)
    static float longitudeToMeters(double startLon, double destLon, double currentLatitude){

        float longitude;

        longitude = (float)((destLon-startLon)*(40075000 * Math.cos(currentLatitude) / 360));
        return longitude;
    }

    // Storing the room file as a function return value..
    // ..to be used as a global variable across all classes
    static String roomFile(){return "rooms.json";}

}