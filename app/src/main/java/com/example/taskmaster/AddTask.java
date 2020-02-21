package com.example.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

public class AddTask extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        TaskDatabase taskDatabase;



        taskDatabase = Room.databaseBuilder(getApplicationContext(), TaskDatabase.class, "task_items").allowMainThreadQueries().build();


        Button addTask = findViewById(R.id.button3);
        addTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                //text inputs
//                EditText input = findViewById(R.id.editText);
//                EditText input1 = findViewById(R.id.editText3);
//
//
//                String tasksText = input.getText().toString();
//                String tasksText1 = input1.getText().toString();
//
//
//                //radio buttons
//                RadioGroup radioButtons = findViewById(R.id.radioGroup);
//                int selectedId = radioButtons.getCheckedRadioButtonId();
//                RadioButton radioButton = findViewById(selectedId);
//                String tasksButtons = radioButton.getText().toString();
//
//
//                Task newTask = new Task(tasksText, tasksText1, tasksButtons);
//                taskDatabase.taskDao().saveTask(newTask);
//
//                Intent takeMeBackToMainPage = new Intent(AddTask.this, MainActivity.class);
//                AddTask.this.startActivity(takeMeBackToMainPage);



                Intent addTaskIntent = new Intent(AddTask.this, MainActivity.class);
                addTaskIntent.putExtra("taskTitle", "Soccer");
                addTaskIntent.putExtra("taskBody", "Mattaus sucks at soccer");
                addTaskIntent.putExtra("taskState", "Complete");
                startActivity(addTaskIntent);
//
//                Context showConfirmation = getApplicationContext();
//                CharSequence confirmationText = "Submitted";
//                int duration = Toast.LENGTH_LONG;
//                Toast toast = Toast.makeText(showConfirmation, confirmationText, duration);
//                toast.show();
//                toast.setGravity(Gravity.TOP|Gravity.RIGHT, 350, 350);
//                Task newTask = new Task(toast)
            }

        });
    }
}
