package com.example.taskmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button addTask = findViewById(R.id.button);
        addTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                Intent addMoreTask = new Intent();
//                TextView textView = findViewById(R.id.button);
//                String addTaskText = textView.getText().toString();
//
//                addMoreTask.putExtra(Intent.EXTRA_TEXT, addTaskText);
//                addMoreTask.setType("text/plain");
//                startActivity(Intent.createChooser(addMoreTask, "Add Task"));

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
    }

}
