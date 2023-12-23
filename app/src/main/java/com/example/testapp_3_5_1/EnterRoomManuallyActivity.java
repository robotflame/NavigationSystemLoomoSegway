package com.example.testapp_3_5_1;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.locomotion.sbv.Base;
import java.io.IOException;
import java.io.InputStream;

// Simple page letting you type room number manually
public class EnterRoomManuallyActivity extends AppCompatActivity {

    EditText enterRoomText;
    Button navigateBtn;
    Button backBtn;

    Base mBase;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_enter_room);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        mBase = Base.getInstance();

        // bindService is required to use the base service
        mBase.bindService(getApplicationContext(), new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {

            }

            @Override
            public void onUnbind(String reason) {

            }
        });

        enterRoomText = findViewById(R.id.enterRoomTextView);
        navigateBtn = findViewById(R.id.navigateBtn);
        backBtn = findViewById(R.id.backBtn);

        navigateBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String destinationRoom = enterRoomText.getText().toString();
                destinationRoom = destinationRoom.toUpperCase().replace(" ", "");

                try {
                    InputStream inputStream = getAssets().open(Functions.roomFile());
                    boolean roomExists = Functions.checkRoomAvailable(destinationRoom, inputStream);

                    if (roomExists) {
                        // Changes to the navigation window
                        Intent navigationPage = new Intent(EnterRoomManuallyActivity.this, NavigationPage.class);
                        navigationPage.putExtra("destinationRoom", destinationRoom);
                        startActivity(navigationPage);

                        // Start navigating
                        // TODO - May be an issue with inputStream in the movement function
                        // TODO - Force all rooms inside an object, then pass the object inside instead of inputStream
                        Navigation.navigate(destinationRoom, inputStream, mBase);
                    }
                    else {
                        String message = String.format("%s does not exist", destinationRoom);
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent mainPage = new Intent(EnterRoomManuallyActivity.this, MainActivity.class);
                startActivity(mainPage);
                finish();
            }
        });
    }
}