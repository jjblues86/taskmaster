package com.example.taskmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class AddTask extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        Button addTask = findViewById(R.id.button3);
        addTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Context showConfirmation = getApplicationContext();
                CharSequence confirmationText = "Submitted";
                int duration = Toast.LENGTH_LONG;
                Toast toast = Toast.makeText(showConfirmation, confirmationText, duration);
                toast.show();
                toast.setGravity(Gravity.TOP|Gravity.RIGHT, 350, 350);
            }
        });
    }
}
