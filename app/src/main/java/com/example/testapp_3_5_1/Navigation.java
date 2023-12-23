package com.example.testapp_3_5_1;

import android.util.Log;

import com.segway.robot.sdk.locomotion.sbv.Base;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.InputStream;
import java.util.concurrent.ExecutionException;


class Navigation{

    // Navigating to the desired room
    static void navigate(String destination, InputStream inputStream, Base mBase){
        try {
            JSONObject dest = MazeMap.getRoom(destination, inputStream);

            double destLat = dest.getDouble("latitude");
            double destLon = dest.getDouble("longitude");
            int destFloor = dest.getInt("floor");

            inputStream.close();

            try {
                JSONObject loomoCoords = new Cisco.getLoomoCoordinates().execute().get();
                double loomoLat = loomoCoords.getDouble("latitude");
                double loomoLon = loomoCoords.getDouble("longitude");
                int loomoFloor = loomoCoords.getInt("floor");
                JSONArray path = new MazeMap.ReceivePathTask().execute(loomoLat, loomoLon, (double)loomoFloor, destLat, destLon, (double)destFloor).get();

                JSONArray coords = Functions.pathCoordinates(path);

                try {
                    NavigationPage.LoomoDrive(mBase, coords);

                } catch (Exception e) {
                    e.printStackTrace();
                }

            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }

        } catch(Exception e){
            String message = String.format("%s not found", destination);
            Log.d("room", message);
        }
    }
}
