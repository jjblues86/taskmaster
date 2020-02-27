package com.example.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.amazonaws.amplify.generated.graphql.CreateTaskMutation;
import com.amazonaws.amplify.generated.graphql.ListTasksQuery;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.SignOutOptions;
import com.amazonaws.mobile.client.UserState;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferService;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import type.CreateTaskInput;

public class MainActivity extends AppCompatActivity {

    private AWSAppSyncClient mAWSAppSyncClient;

    private static final String TAG = "jj.main";
    List<Task> tasks;
    TaskDatabase taskDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //retrieve data from DynamoDb
        mAWSAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                .build();
        getTaskItems();

        //local database
        taskDatabase = Room.databaseBuilder(getApplicationContext(), TaskDatabase.class, "task_items").allowMainThreadQueries().build();

        this.tasks = new ArrayList<Task>();


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
//                Intent activity for AddTask button
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
        //s3 transferservice
        getApplicationContext().startService(new Intent(getApplicationContext(), TransferService.class));

        //Button for signOut and method to reroute you back to the signup page
        Button signOut = findViewById(R.id.signout);
        signOut.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AWSMobileClient.getInstance().signOut(SignOutOptions.builder().signOutGlobally(true).build(), new Callback<Void>() {
                    @Override
                    public void onResult(final Void result) {
                        Log.d(TAG, "signingout");
                        Intent signOutIntent = new Intent(MainActivity.this, MainActivity.class);
                        MainActivity.this.startActivity(signOutIntent);
                    }

                    @Override
                    public void onError(Exception e) {

                    }
                });
            }
    });

        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {
                    @Override
                    public void onResult(UserStateDetails userStateDetails) {
                        Log.i("INIT", "onResult: " + userStateDetails.getUserState());
                    }
                    @Override
                    public void onError(Exception e) {
                        Log.e("INIT", "Initialization error.", e);
                    }
                });
        AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {
                    @Override
                    public void onResult(UserStateDetails userStateDetails) {
                        Log.i("INIT", "onResult: " + userStateDetails.getUserState());
                        if(userStateDetails.getUserState().equals(UserState.SIGNED_OUT)){
                            // 'this' refers the the current active activity
                            AWSMobileClient.getInstance().showSignIn(MainActivity.this, new Callback<UserStateDetails>() {
                                @Override
                                public void onResult(UserStateDetails result) {
                                    Log.d(TAG, "onResult: " + result.getUserState());

                                    if(result.getUserState().equals(UserState.SIGNED_IN)){

                                        uploadWithTransferUtility();
                                    }

                                }
                                @Override
                                public void onError(Exception e) {
                                    Log.e(TAG, "onError: ", e);
                                }
                            });
                        }
                    }
                    @Override
                    public void onError(Exception e) {
                        Log.e("INIT", "Initialization error.", e);
                    }
                });

//        AWSMobileClient.getInstance().signOut(SignOutOptions.builder().signOutGlobally(true).build(), new Callback<Void>() {
//            @Override
//            public void onResult(final Void result) {
//                Log.d(TAG, "signed-out");
//
//            }
//            @Override
//            public void onError(Exception e) {
//                Log.e(TAG, "sign-out error", e);
//            }
//        });


    }
//    //Intent activity for setting
    public void whenSettingsButtonIsPressed(View v){
        Intent settingsPage = new Intent(this, SettingsActivity.class);
        MainActivity.this.startActivity(settingsPage);
    }
    @Override
    protected void onResume(){
        super.onResume();
        Log.i(TAG, "resumed");
//        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String enteredUsername =  AWSMobileClient.getInstance().getUsername();
        TextView textView = findViewById(R.id.textView7);
//        String settingsUsername = textView.getText().toString();
        textView.setText(enteredUsername);
        textView.setVisibility(View.VISIBLE);
        if(getIntent().getStringExtra("taskTitle") != null)
        {
            String addTaskIntentTitle = getIntent().getStringExtra("taskTitle");
            String addTaskIntentBody = getIntent().getStringExtra("taskBody");
            String addTaskIntentState = getIntent().getStringExtra("taskState");
            runTaskMutation(addTaskIntentTitle, addTaskIntentBody, addTaskIntentState);
        }
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
            getTaskItems();
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e(TAG, e.toString());
        }
    };
    //this method enables me to query data stored in dynamodb to render on my front page
    public void getTaskItems()
    {
        Log.i(TAG, "Did we make it into getTaskItems");

        mAWSAppSyncClient.query(ListTasksQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.NETWORK_ONLY)
                .enqueue(tasksCallback);
    }
    private GraphQLCall.Callback<ListTasksQuery.Data> tasksCallback = new GraphQLCall.Callback<ListTasksQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListTasksQuery.Data> response)
        {
            Log.i(TAG, response.data().listTasks().items().toString());

            if(tasks.size() == 0 || response.data().listTasks().items().size() != tasks.size()){

                tasks.clear();

                for(ListTasksQuery.Item item : response.data().listTasks().items()){
                    Task addTask = new Task(item.title(), item.body(), item.state());
                    tasks.add(addTask);
                }
                Handler handler = new Handler(Looper.getMainLooper()){
                    @Override
                    public void handleMessage(Message inputMessage){
                        RecyclerView recyclerView = findViewById(R.id.fragment2);
                        recyclerView.getAdapter().notifyDataSetChanged();
                    }
                };
                handler.obtainMessage().sendToTarget();
            }
        }
        @Override
        public void onFailure(@Nonnull ApolloException e)
        {
            Log.e(TAG, e.toString());
            taskDatabase.taskDao().getAll();
        }
    };

    public void uploadWithTransferUtility() {

        TransferUtility transferUtility =
                TransferUtility.builder()
                        .context(getApplicationContext())
                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                        .s3Client(new AmazonS3Client(AWSMobileClient.getInstance()))
                        .build();

        File file = new File(getApplicationContext().getFilesDir(), "sample.txt");
        try {
            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            writer.append("Howdy Jerome!");
            writer.close();
        }
        catch(Exception e) {
            Log.e(TAG, e.getMessage());
        }

        TransferObserver uploadObserver =
                transferUtility.upload(
                        "public/sample.txt",
                        new File(getApplicationContext().getFilesDir(),"sample.txt"));

        // Attach a listener to the observer to get state update and progress notifications
        uploadObserver.setTransferListener(new TransferListener() {

            @Override
            public void onStateChanged(int id, TransferState state) {
                if (TransferState.COMPLETED == state) {
                    // Handle a completed upload.
                }
            }

            @Override
            public void onProgressChanged(int id, long bytesCurrent, long bytesTotal) {
                float percentDonef = ((float) bytesCurrent / (float) bytesTotal) * 100;
                int percentDone = (int)percentDonef;

                Log.d(TAG, "ID:" + id + " bytesCurrent: " + bytesCurrent
                        + " bytesTotal: " + bytesTotal + " " + percentDone + "%");
            }

            @Override
            public void onError(int id, Exception ex) {
                // Handle errors
            }

        });

        // If you prefer to poll for the data, instead of attaching a
        // listener, check for the state and progress in the observer.
        if (TransferState.COMPLETED == uploadObserver.getState()) {
            // Handle a completed upload.
        }

        Log.d(TAG, "Bytes Transferred: " + uploadObserver.getBytesTransferred());
        Log.d(TAG, "Bytes Total: " + uploadObserver.getBytesTotal());
    }
}


