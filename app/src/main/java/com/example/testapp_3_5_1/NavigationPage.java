package com.example.testapp_3_5_1;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.segway.robot.algo.Pose2D;
import com.segway.robot.algo.minicontroller.CheckPoint;
import com.segway.robot.algo.minicontroller.CheckPointStateListener;
import com.segway.robot.sdk.locomotion.sbv.Base;

import org.json.JSONArray;
import org.json.JSONException;

// Simple view telling you it is navigating to your desired room
// a cancel button which allows you to cancel the navigation (currently not working properly)
// TODO - implement the cancel button correctly
public class NavigationPage extends AppCompatActivity {

    Button cancelBtn;
    TextView navigationTextView;
    Base mBase;

    private static boolean isDriving;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_navigation_page);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        cancelBtn = findViewById(R.id.cancelBtn);
        navigationTextView = findViewById(R.id.navigationToTextView);

        // Receiving the destination room string
        Intent i = getIntent();
        String destinationRoom = i.getStringExtra("destinationRoom");
        String destinationRoomMessage = String.format("Navigating to %s", destinationRoom);

        navigationTextView.setText(destinationRoomMessage);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Stops the loomo and goes back to previous screen
                mBase.clearCheckPointsAndStop();
                finish();
            }
        });
    }

    // Having the loomoDrive function in this class as it is easier to connect with the cancel button
    // TODO - Make this work properly. Loomo does currently NOT drive
    public static void LoomoDrive(final Base mBase, JSONArray coordinates) throws JSONException {

        mBase.setControlMode(Base.CONTROL_MODE_NAVIGATION);

        double x1, y1, x2, y2;
        float x, y;

        for (int i = 1; i < coordinates.length(); i++) {
            mBase.cleanOriginalPoint();
            Pose2D pose2D = mBase.getOdometryPose(-1);
            mBase.setOriginalPoint(pose2D);

            JSONArray coords = coordinates.getJSONArray(i);     // Looping through all coordinates
            JSONArray c1 = coords.getJSONArray(0);              // Receiving the current coordinates
            JSONArray c2 = coords.getJSONArray(1);              // Receiving the destination coordinate

            x1 = c1.getDouble(0);     // Longitude
            y1 = c1.getDouble(1);     // Latitude

            x2 = c2.getDouble(0);     // Longitude
            y2 = c2.getDouble(1);     // Latitude

            // Receiving x and y next checkpoints in meters from current checkpoint
            x = Functions.longitudeToMeters(x1, x2, y2);
            y = Functions.latitudeToMeters(y1, y2);

            mBase.addCheckPoint(x, y);

            isDriving = true;

            while (isDriving){
                mBase.setOnCheckPointArrivedListener(new CheckPointStateListener() {
                    @Override
                    public void onCheckPointArrived(CheckPoint checkPoint, Pose2D realPose, boolean isLast) {
                        isDriving = false;
                    }

                    @Override
                    public void onCheckPointMiss(CheckPoint checkPoint, Pose2D realPose, boolean isLast, int reason) {

                    }
                });
            }
        }
    }
}