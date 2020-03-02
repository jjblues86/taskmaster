package com.example.taskmaster;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
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
import com.amazonaws.mobileconnectors.pinpoint.PinpointConfiguration;
import com.amazonaws.mobileconnectors.pinpoint.PinpointManager;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferService;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nonnull;
import type.CreateTaskInput;

public class MainActivity extends AppCompatActivity {

    private AWSAppSyncClient mAWSAppSyncClient;
    static String CHANNEL_ID = "kay";

    private static final String TAG = "jj.main";
    private static PinpointManager pinpointManager;
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
        // Initialize PinpointManager
        getPinpointManager(getApplicationContext());

        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "Channel";
            String description = "description";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);


        }

        // Create an explicit intent for an Activity in your app
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, 0);
        //creating notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle("Do not forget your meeting")
                .setContentText("This is important")
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(this);

        // notificationId is a unique int for each notification that you must define
        notificationManager.notify((int)(Math.random() * 100.0), builder.build());



        //local database
        taskDatabase = Room.databaseBuilder(getApplicationContext(), TaskDatabase.class, "task_items").allowMainThreadQueries().build();

        this.tasks = new ArrayList<Task>();


        //this.tasks = taskDatabase.taskDao().getAll();
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


        //this allows me to implement auth in the app
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


    }
    //Intent activity for setting
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
            String addTaskIntentImage = getIntent().getStringExtra("taskImage");
            runTaskMutation(addTaskIntentTitle, addTaskIntentBody, addTaskIntentState, addTaskIntentImage);
        }
    }
    //this method accesses the schema
    public void runTaskMutation(String title, String body, String state, String file ){
        CreateTaskInput createTaskInput = CreateTaskInput.builder().
                title(title).
                body(body).
                state(state).
                image(file).build();

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
                    Task addTask = new Task(item.title(), item.body(), item.state(), item.image());
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
//            taskDatabase.taskDao().getAll();
        }
    };
    public static PinpointManager getPinpointManager(final Context applicationContext) {
        if (pinpointManager == null) {
            final AWSConfiguration awsConfig = new AWSConfiguration(applicationContext);
            AWSMobileClient.getInstance().initialize(applicationContext, awsConfig, new Callback<UserStateDetails>() {
                @Override
                public void onResult(UserStateDetails userStateDetails) {
                    Log.i("INIT", userStateDetails.getUserState().toString());
                }

                @Override
                public void onError(Exception e) {
                    Log.e("INIT", "Initialization error.", e);
                }
            });

            PinpointConfiguration pinpointConfig = new PinpointConfiguration(
                    applicationContext,
                    AWSMobileClient.getInstance(),
                    awsConfig);

            pinpointManager = new PinpointManager(pinpointConfig);

            FirebaseInstanceId.getInstance().getInstanceId()
                    .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull com.google.android.gms.tasks.Task<InstanceIdResult> task) {
                            if (!task.isSuccessful()) {
                                Log.w(TAG, "getInstanceId failed", task.getException());
                                return;
                            }
                            final String token = task.getResult().getToken();
                            Log.d(TAG, "Registering push notifications token: " + token);
                            pinpointManager.getNotificationClient().registerDeviceToken(token);

                        }

//                        @Override
//                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
//                            if (!task.isSuccessful()) {
//                                Log.w(TAG, "getInstanceId failed", task.getException());
//                                return;
//                            }
//                            final String token = task.getResult().getToken();
//                            Log.d(TAG, "Registering push notifications token: " + token);
//                            pinpointManager.getNotificationClient().registerDeviceToken(token);
//                        }
                    });
        }
        return pinpointManager;
    }

}


