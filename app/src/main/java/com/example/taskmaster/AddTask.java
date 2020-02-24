package com.example.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.amazonaws.amplify.generated.graphql.CreateTaskMutation;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import java.util.List;

import javax.annotation.Nonnull;

import type.CreateTaskInput;

public class AddTask extends AppCompatActivity {

    private AWSAppSyncClient mAWSAppSyncClient;

    private static final String TAG = "jj.main";
    private List<Task> tasks;


    TaskDatabase taskDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);


        mAWSAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                .build();


        taskDatabase = Room.databaseBuilder(getApplicationContext(), TaskDatabase.class, "task_items").allowMainThreadQueries().build();


        Button addTask = findViewById(R.id.button3);
        addTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//                //text inputs
                EditText input = findViewById(R.id.editText);
                EditText input1 = findViewById(R.id.editText3);


                String tasksText = input.getText().toString();
                String tasksText1 = input1.getText().toString();

//                //radio buttons
                RadioGroup radioButtons = findViewById(R.id.radioGroup);
                int selectedId = radioButtons.getCheckedRadioButtonId();
                RadioButton radioButton = findViewById(selectedId);
                String tasksButtons = radioButton.getText().toString();

                Task newTask = new Task(tasksText, tasksText1, tasksButtons);
                taskDatabase.taskDao().saveTask(newTask);

                runTaskMutation(tasksText,tasksText1,tasksButtons);
            }

        });
    }

    @Override
    protected void onResume(){
        super.onResume();
        tasks = taskDatabase.taskDao().getAll();
        TextView allTasksCount = findViewById(R.id.totalTasks);
        allTasksCount.setText("Total Tasks: " + tasks.size());
        Log.i(TAG, "resumed");
    }

    public void runTaskMutation(String title, String body, String state){
        CreateTaskInput createTaskInput = CreateTaskInput.builder().
                title(title).
                body(body).
                state(state).build();

        mAWSAppSyncClient.mutate(CreateTaskMutation.builder().input(createTaskInput).build())
                .enqueue(taskMutationCallback);//performs the callback when we want the data gets inserted

    }

    private GraphQLCall.Callback<CreateTaskMutation.Data> taskMutationCallback = new GraphQLCall.Callback<CreateTaskMutation.Data>() {
        @Override
        public void onResponse(@Nonnull Response<CreateTaskMutation.Data> response)
        {
            Log.i(TAG, "Added Task");
            Intent takeMeBackToMainPage = new Intent(AddTask.this, MainActivity.class);
            AddTask.this.startActivity(takeMeBackToMainPage);
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e(TAG, e.toString());
        }
    };


    @Override
    protected void onStart(){
        super.onStart();
        Log.i(TAG, "started");
    }

}



