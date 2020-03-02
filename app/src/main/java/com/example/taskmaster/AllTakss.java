package com.example.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.amplify.generated.graphql.ListTasksQuery;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.appsync.fetcher.AppSyncResponseFetchers;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;
import com.example.taskmaster.MyTaskRecyclerViewAdapter.ViewHolder;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;

public class AllTakss extends AppCompatActivity{

    private AWSAppSyncClient mAWSAppSyncClient;
    private static final String TAG = "jj.main";
    private List<Task> tasks;

    TaskDatabase taskDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_takss);

        mAWSAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                .build();

        this.tasks = new ArrayList<Task>();
        getTaskItems();

        RecyclerView recyclerView = findViewById(R.id.fragment);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(new MyTaskRecyclerViewAdapter(this.tasks, null));

    }
//    @Override
//    public void onClick(Task task){
//        Toast.makeText(getApplicationContext(), task.getBody(), Toast.LENGTH_LONG).show();
//    }

    @Override
    protected void onResume() {
        super.onResume();
//        tasks = taskDatabase.taskDao().getAll();
//        TextView allTasks = findViewById(R.id.fragment);
//        allTasks.setText("Total Tasks: );
        Log.i(TAG, "resumed");
    }

    //this method enables me to query data stored in dynamodb to render on my front page
    public void getTaskItems() {
        Log.i(TAG, "Did we make it into getTaskItems");

        mAWSAppSyncClient.query(ListTasksQuery.builder().build())
                .responseFetcher(AppSyncResponseFetchers.NETWORK_ONLY)
                .enqueue(tasksCallback);
    }

    private GraphQLCall.Callback<ListTasksQuery.Data> tasksCallback = new GraphQLCall.Callback<ListTasksQuery.Data>() {
        @Override
        public void onResponse(@Nonnull Response<ListTasksQuery.Data> response) {
            Log.i(TAG, response.data().listTasks().items().toString());

            if (tasks.size() == 0 || response.data().listTasks().items().size() != tasks.size()) {

                tasks.clear();

                for (ListTasksQuery.Item item : response.data().listTasks().items()) {
                    Task addTask = new Task(item.title(), item.body(), item.state(), item.image());
                    tasks.add(addTask);
                }
                Handler handler = new Handler(Looper.getMainLooper()) {
                    @Override
                    public void handleMessage(Message inputMessage) {
                        RecyclerView recyclerView = findViewById(R.id.fragment);
                        recyclerView.getAdapter().notifyDataSetChanged();
                    }
                };
                handler.obtainMessage().sendToTarget();
            }
        }

        @Override
        public void onFailure(@Nonnull ApolloException e) {
            Log.e(TAG, e.toString());
            taskDatabase.taskDao().getAll();
        }
    };
}
