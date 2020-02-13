package com.example.taskmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class SettingsActivity extends AppCompatActivity {

    static final String TAG = "jj.main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Button saveUsername = findViewById(R.id.button4);
        saveUsername.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {

                Log.d(TAG, "user entered a username");

        TextView textView = findViewById(R.id.editText5);
        String settingsUsername = textView.getText().toString();
//        textView.setOnClickListener();
                //when a username enters a name and hit the save button this should
                // save their username to shared preferences
//                SettingsActivity.this.getSharedPreferences();
                SharedPreferences savedData = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                SharedPreferences.Editor editor = savedData.edit();
                editor.putString("userName", settingsUsername);
                editor.apply();
            }

        });
            }

    }
