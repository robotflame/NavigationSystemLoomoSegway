package com.example.testapp_3_5_1;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.segway.robot.sdk.base.bind.ServiceBinder;
import com.segway.robot.sdk.locomotion.sbv.Base;

import java.io.InputStream;
import java.util.ArrayList;


// Run on 800x480 pixels, rotate landscape -> this is size and direction of the loomo screen
public class MainActivity extends AppCompatActivity {

    private final int REQUEST_CODE = 1000;
    private boolean room_is_found = false;

    Button enterRoomManuallyBtn;
    Base mBase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // Configuring the base
        mBase = Base.getInstance();
        mBase.bindService(getApplicationContext(), new ServiceBinder.BindStateListener() {
            @Override
            public void onBind() {

            }

            @Override
            public void onUnbind(String reason) {

            }
        });

        // TODO - Speech recognizer currently does not work on Loomo
        // Calling the speak function
        speak();

        enterRoomManuallyBtn = findViewById(R.id.enterRoomManuallyBtn);
        enterRoomManuallyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent manualPage = new Intent(MainActivity.this, EnterRoomManuallyActivity.class);
                startActivity(manualPage);
            }
        });

    }

    private void speak(){
        try {
            Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "no-NO");
            intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Where would you like to go?");

            startActivityForResult(intent, REQUEST_CODE);

        } catch(ActivityNotFoundException e){
            Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
        }
/*
        // Attempting to open the install page for the package if it does not exist
        String appPackageName = "android.speech.RecognizerIntent";
        try{
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://android.com/details?id=" + appPackageName)));
        } catch(android.content.ActivityNotFoundException e){
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + appPackageName)));
        }
*/
    }

    // onResults for the speak function
    // Re-calls speak() unless a valid room is found or the user clicks out of the speech window
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE) {
            if (resultCode == RESULT_OK && null != data) {
                ArrayList<String> result = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                // Making the string upper-case and removing spaces
                // This is needed as the room names contains capital letters and no spaces
                String destinationRoom = result.get(0).toUpperCase().replace(" ", "");
                try {
                    InputStream inputStream = getAssets().open(Functions.roomFile());
                    boolean roomExists = Functions.checkRoomAvailable(destinationRoom, inputStream);

                    // Checks if the room exists, if not, re-calls the speak() function
                    if (roomExists) {
                        // Switching page to the navigation page
                        Intent navigationPage = new Intent(MainActivity.this, NavigationPage.class);
                        navigationPage.putExtra("destinationRoom", destinationRoom);    // Being able to use the variable in the next page
                        startActivity(navigationPage);

                        // Starting the navigation
                        Navigation.navigate(destinationRoom, inputStream, mBase);
                    }
                    else {
                        speak();
                    }

                } catch (Exception e){
                    e.printStackTrace();
                }
            }
        }
    }
}