package com.example.testapp_3_5_1;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;

public class MazeMap {

    // Generating all rooms and writes all JSONObjects to file
    // NB! This was used in visual studio code and generated there
    // As files needs to be in assets folder in android studio, this code would need a slight change
    private static void GenerateRooms() throws Exception{

        int LOWEST = 675450;      // Lowest poiID needed to loop through all rooms
        int HIGHEST = 677000;     // Highest poiID needed to loop through all rooms

        JSONArray Rooms = new JSONArray();

        for (int i = LOWEST; i < HIGHEST; i++){
            try{
                String roomID = String.format("%s", i);
                String poiURL = String.format("https://api.mazemap.com/api/pois/%s/?srid=4326", roomID);
                URL url = new URL(poiURL);

                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

                String inputLine;
                StringBuffer content = new StringBuffer();
                while ((inputLine = in.readLine()) != null)
                    content.append(inputLine);
                in.close();

                JSONObject jsonObject = new JSONObject(content.toString());

                // Receiving the wanted information, storing them in variables
                int campusID = jsonObject.getInt("campusId");
                String poiID = roomID;
                String roomName = jsonObject.getString("identifier");
                Integer floor = jsonObject.getInt("floorName");
                JSONObject point = jsonObject.getJSONObject("point");
                JSONArray coords = point.getJSONArray("coordinates");
                double longitude = coords.getDouble(0);
                double latitude = coords.getDouble(1);

                JSONObject room = new JSONObject();
                // Only want rooms from campusID = 225  -->  Campus Grimstad
                if (campusID == 225){
                    room.put("poiID", poiID);             // setting the current roomID (poiID) of the room
                    room.put("name", roomName);           // setting the current name of the room, e.g. A2020
                    room.put("floor", floor);             // setting the floor the current room is on
                    room.put("longitude", longitude);     // setting the current longitude value of the room
                    room.put("latitude", latitude);       // setting the current latitude value of the room
                }

                Rooms.put(room);
            }
            catch(Exception e){
                System.out.println(String.format("Room %s does not exist", i));
                e.printStackTrace();
            }
        }

        // The main entrance ID was way out of range, and therefore had to hardcode it
        // Not the best way of doing it, but we might need this place in case of a tour guide needed, starting from the entrance hall
        JSONObject mainEntrance = new JSONObject();
        mainEntrance.put("poiID", 1000696532);
        mainEntrance.put("name", "B1021");
        mainEntrance.put("floor", 1);
        mainEntrance.put("longitude", 8.576996758376989);
        mainEntrance.put("latitude", 58.334549452535335);
        Rooms.put(mainEntrance);


        try (FileWriter fw = new FileWriter("rooms.json")) {
            fw.write(Rooms.toString(4));  // Need to do the .toString(4) to make it form like a JSON object
            fw.flush();
        } catch (IOException e) {
            System.out.println("Error reading to file");
        }

    }

    // Generates all rooms if the file does not exist or if it is empty
    // Needs slight change if being used in Android Studio
    // Does not need to be used as the rooms already exist in the file
    // TODO - Rewrite to Android Studio.. This was for Visual Studio Code, where files were not located in an assets folder
    public void RoomGenerator() throws Exception{
        File rooms = new File(Functions.roomFile());
        if (rooms.exists()){
            if (rooms.length() == 0){
                GenerateRooms();
            }
            else{
                System.out.println("rooms.json already exists");
            }
        }
        else{
            GenerateRooms();
        }
    }

    // Returning the room with the given parameter name as a JSONObject
    static JSONObject getRoom(String destRoom, InputStream inputStream) {
        try {
            // Read the contents of the file into a string
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            StringBuilder stringBuilder = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line);
            }
            String jsonString = stringBuilder.toString();

            // Create a JSONArray from the JSON file
            JSONArray jsonArray = new JSONArray(jsonString);

            // Searching for the room where the room name == function parameter
            JSONObject roomInformation = null;
            for (int i = 0; i < jsonArray.length(); i++) {
                if (jsonArray.get(i) instanceof JSONObject) {
                    JSONObject tempObject = jsonArray.getJSONObject(i);

                    // If the room with the given name exists, setting the return object equal the given room
                    if (tempObject.has("name") && tempObject.getString("name").equals(destRoom)) {
                        roomInformation = tempObject;
                        break;
                    }
                }
            }

            // If roomInformation != 0, we return the roomInformation object
            // This will be the room we searched for by string name
            if (roomInformation != null) {
                return roomInformation;

            } else {
                System.out.printf("%s was not found.. Please check if you have the correct room!%n", destRoom);
                return new JSONObject();
            }
        } catch (Exception e) {
            e.printStackTrace();
            return new JSONObject();
        }
    }

    // Method used for receiving path description from A to B
    static class ReceivePathTask extends AsyncTask<Double, Void, JSONArray> {

        @Override
        protected JSONArray doInBackground(Double... params) {
            Double startLatitude = params[0];
            Double startLongitude = params[1];
            Double startFloor = params[2];
            Double destLatitude = params[3];
            Double destLongitude = params[4];
            Double destFloor = params[5];

            String link = String.format("https://routing.mazemap.com/routing/path/?srid=4326&hc=true&sourcelat=%s&sourcelon=%s&targetlat=%s&targetlon=%s&sourcez=%s&targetz=%s&lang=en&distanceunitstype=metric&mode=PEDESTRIAN",
                    startLatitude, startLongitude, destLatitude, destLongitude, Math.round(startFloor), Math.round(destFloor));

            try {
                URL url = new URL(link);
                BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

                String inputLine;
                StringBuilder content = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    content.append(inputLine);
                }
                in.close();

                JSONObject path = new JSONObject(content.toString());
                JSONArray p = new JSONArray();
                p.put(path);
                return p;

            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }
    }
}
