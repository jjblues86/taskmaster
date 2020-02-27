package com.example.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.amplify.generated.graphql.CreateTaskMutation;
import com.amazonaws.mobile.client.AWSMobileClient;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

import javax.annotation.Nonnull;

import type.CreateTaskInput;

public class AddTask extends AppCompatActivity {

    private AWSAppSyncClient mAWSAppSyncClient;
    private static final int OPEN_DOCUMENT_CODE = 2;
    public Uri imageUri;

    private static final String TAG = "jj.main";
    private List<Task> tasks;
    ImageView imageView;


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

        //upload photos
        Button uploadButton = findViewById(R.id.uploadButton);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, OPEN_DOCUMENT_CODE);


            }
        });

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        if (requestCode == OPEN_DOCUMENT_CODE && resultCode == RESULT_OK) {
            if (resultData != null) {
                // this is the image selected by the user
                Uri imageUri = resultData.getData();
                ImageView imageContainer = findViewById(R.id.imageView);
                imageContainer.setImageURI(imageUri);
                imageContainer.setVisibility(View.VISIBLE);

                uploadWithTransferUtility(convertUriToFilePath(imageUri));
            }
        }
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

    private String convertUriToFilePath(Uri uri) {
        Log.i("filepath", uri.toString());
        String[] filePathColumn = {MediaStore.Images.Media.DATA};
        Cursor cursor = getContentResolver().query(uri,
                filePathColumn, null, null, null);
        cursor.moveToFirst();
        int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
        // String filePath contains the path of selected file
        String filePath = cursor.getString(columnIndex);
        Log.i("filepath", "" + filePath);
        cursor.close();
        return filePath;
    }

    public void uploadWithTransferUtility(String filePath) {
        if(filePath == null){
            Toast.makeText(this, "No file found", Toast.LENGTH_LONG).show();
            return;
        }

        TransferUtility transferUtility =
                TransferUtility.builder()
                        .context(getApplicationContext())
                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                        .s3Client(new AmazonS3Client(AWSMobileClient.getInstance()))
                        .build();

        File file = new File(filePath);

//        try {
//            BufferedWriter writer = new BufferedWriter(new FileWriter(file));
//            writer.append("Howdy Jerome!");
//            writer.close();
//        }
//        catch(Exception e) {
//            Log.e(TAG, e.getMessage());
//        }

        TransferObserver uploadObserver =
                transferUtility.upload(
                        "public/image.png", file);

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



