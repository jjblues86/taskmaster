package com.example.taskmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "mnf.main";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button addTask = findViewById(R.id.button);
        addTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Intent activity for AddTask button
                Intent goToAddTask = new Intent(MainActivity.this, AddTask.class);
                MainActivity.this.startActivity(goToAddTask);

            }
        });

        //Intent activity for AllTasks button
        Button addAllTasks = findViewById(R.id.button2);
        addAllTasks.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Intent activity for AddTask button
                Intent goToAllTasks = new Intent(MainActivity.this, AllTakss.class);
                MainActivity.this.startActivity(goToAllTasks);

            }
        });

        Button codingButton = findViewById(R.id.button9);
        codingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Intent activity for Coding
                Intent goToCodingTask = new Intent(MainActivity.this, TaskDetailActivity.class);
                goToCodingTask.putExtra("buttonName", "Coding");
                MainActivity.this.startActivity(goToCodingTask);
            }
        });

        Button cookingButton = findViewById(R.id.button8);
        cookingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Intent activity for cooking
                Intent goToCookingTask = new Intent(MainActivity.this, TaskDetailActivity.class);
//                Button cookingButton = findViewById(R.id.button8);
                goToCookingTask.putExtra("buttonName", "Cooking" );
                MainActivity.this.startActivity(goToCookingTask);
            }
        });

        Button soccerButton = findViewById(R.id.button7);
        soccerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //Intent activity for soccer
                Intent goToSoccerTask = new Intent(MainActivity.this, TaskDetailActivity.class);
                goToSoccerTask.putExtra("buttonName", "Soccer" );
                MainActivity.this.startActivity(goToSoccerTask);
            }
        });


    }
    //Intent activity for setting
    public void whenSettingsButtonIsPressed(View v){
        Intent settingsPage = new Intent(this, SettingsActivity.class);
        MainActivity.this.startActivity(settingsPage);

    }
    @Override
    protected void onStart(){
        super.onStart();
        Log.i(TAG, "started");
    }

    @Override
    protected void onResume(){
        super.onResume();
        Log.i(TAG, "resumed");
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String enteredUsername = sharedPreferences.getString("userName", "default");
        TextView textView = findViewById(R.id.textView7);
//        String settingsUsername = textView.getText().toString();
        textView.setText(enteredUsername);
        textView.setVisibility(View.VISIBLE);
//        if(enteredUsername.equals("userName"))

    }

//    @Override
//    protected void onPaused(){
//        super.onPause();
//        Log.i(TAG, "paused");
//    }

}


