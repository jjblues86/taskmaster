package com.example.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.amazonaws.amplify.generated.graphql.CreateTaskMutation;
import com.amazonaws.amplify.generated.graphql.ListTasksQuery;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.util.List;

import javax.annotation.Nonnull;

import type.CreateTaskInput;

public class MainActivity extends AppCompatActivity {

    private AWSAppSyncClient mAWSAppSyncClient;

    private static final String TAG = "jj.main";
    private List<Task> tasks;

    TaskDatabase taskDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAWSAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                .build();

        taskDatabase = Room.databaseBuilder(getApplicationContext(), TaskDatabase.class, "task_items").allowMainThreadQueries().build();

        this.tasks = taskDatabase.taskDao().getAll();
        for (Task item : tasks){
            Log.i(TAG, item.body + item.title + item.state);
        }

        RecyclerView recyclerView = findViewById(R.id.fragment2);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new MyTaskRecyclerViewAdapter(this.tasks, null));


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


//        Button codingButton = findViewById(R.id.button9);
//        codingButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                //Intent activity for Coding
//                Intent goToCodingTask = new Intent(MainActivity.this, TaskDetailActivity.class);
//                goToCodingTask.putExtra("buttonName", "Coding");
//                MainActivity.this.startActivity(goToCodingTask);
//            }
//        });
//
//        Button cookingButton = findViewById(R.id.button8);
//        cookingButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                //Intent activity for cooking
//                Intent goToCookingTask = new Intent(MainActivity.this, TaskDetailActivity.class);
//                Button cookingButton = findViewById(R.id.button8);
//                goToCookingTask.putExtra("buttonName", "Cooking" );
//                MainActivity.this.startActivity(goToCookingTask);
//            }
//        });
//
//        Button soccerButton = findViewById(R.id.button7);
//        soccerButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//                //Intent activity for soccer
//                Intent goToSoccerTask = new Intent(MainActivity.this, TaskDetailActivity.class);
//                goToSoccerTask.putExtra("buttonName", "Soccer" );
//                MainActivity.this.startActivity(goToSoccerTask);
//            }
//        });


    }

//    public void runMutation(){
//        CreateAddTaskInput
//
//    }


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
            Log.i("Results", "Added Task");
            getTaskItems();
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e("Error", e.toString());
        }
    };


    public void getTaskItems()
    {
        mAWSAppSyncClient.query(ListTasksQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.CACHE_AND_NETWORK)
                .enqueue(tasksCallback);

    }


    private GraphQLCall.Callback<ListTasksQuery.Data> tasksCallback = new GraphQLCall.Callback<ListTasksQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListTasksQuery.Data> response)
        {
            Log.i("Results", response.data().listTasks().items().toString());
//            runOnUiThread(new Runnable()
//            {
//                @Override
//                public void run()
//                {
//                    tasks.clear();
//                    List<ListTasksQuery.Item> items = response.data().listTasks().items();
//                    tasks.clear();
//                    for(ListTasksQuery.Item item : items)
//                    {
//
//                    }
//                    //adapter.notifyDataSetChanged();
//                }
//            });
        }
        @Override
        public void onFailure(@Nonnull ApolloException e)
        {
            Log.e("ERROR", e.toString());
            // Toast.makeText(getApplicationContext(), "DynamoDB Query ERROR", Toast.LENGTH_SHORT).show();
        }
    };



    //Intent activity for setting
    public void whenSettingsButtonIsPressed(View v){
        Intent settingsPage = new Intent(this, SettingsActivity.class);
        MainActivity.this.startActivity(settingsPage);
    }
    @Override
    protected void onStart(){
        super.onStart();
        getTaskItems();
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
//        runTaskMutation("title", "body", "new");
        if(enteredUsername.equals("userName"))
        if(getIntent().getStringExtra("taskTitle") != null)
        {
            String addTaskIntentTitle = getIntent().getStringExtra("taskTitle");
            String addTaskIntentBody = getIntent().getStringExtra("taskBody");
            String addTaskIntentState = getIntent().getStringExtra("taskState");
            runTaskMutation(addTaskIntentTitle, addTaskIntentBody, addTaskIntentState);
        }
    }

//    @Override
//    protected void onPaused(){
//        super.onPause();
//        Log.i(TAG, "paused");
//    }


//    @Override
//    protected void onNewIntent(Intent intent) {
//        super.onNewIntent(intent);
//
//        String addTaskIntentTitle = getIntent().getStringExtra("taskTitle");
//        String addTaskIntentBody = getIntent().getStringExtra("taskBody");
//        String addTaskIntentState = getIntent().getStringExtra("taskState");
////        runTaskMutation(addTaskIntentTitle, addTaskIntentBody, addTaskIntentState);
//    }
}


