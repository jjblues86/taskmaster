package com.example.taskmaster;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import com.amazonaws.mobile.client.Callback;
import com.amazonaws.mobile.client.UserStateDetails;
import com.amazonaws.mobile.config.AWSConfiguration;
import com.amazonaws.mobileconnectors.appsync.AWSAppSyncClient;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferService;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.apollographql.apollo.GraphQLCall;
import com.apollographql.apollo.api.Response;
import com.apollographql.apollo.exception.ApolloException;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nonnull;

import type.CreateTaskInput;

public class AddTask extends AppCompatActivity {

    private AWSAppSyncClient mAWSAppSyncClient;
    private static final int OPEN_DOCUMENT_CODE = 2;

    private static final String TAG = "jj.main";
    private List<Task> tasks;
    ImageView imageView;
    private Uri uri;
    private String file;


    TaskDatabase taskDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);


        mAWSAppSyncClient = AWSAppSyncClient.builder()
                .context(getApplicationContext())
                .awsConfiguration(new AWSConfiguration(getApplicationContext()))
                .build();


         imageView = findViewById(R.id.imageView);

            // Get the intent that started this activity
        Intent intent = getIntent();
        String type = intent.getType();
            // Figure out what to do based on the intent type
            if (type != null && type.contains("image/")) {
                // Handle intents with image data ...
                uri = (Uri) intent.getParcelableExtra(Intent.EXTRA_STREAM);
                if(uri != null){
                    imageView.setVisibility(View.VISIBLE);
                    imageView.setImageURI(uri);
//                    imageSelected(uri);
                }

            }



//        taskDatabase = Room.databaseBuilder(getApplicationContext(), TaskDatabase.class, "task_items").allowMainThreadQueries().build();


        Log.i(TAG, "onCreate");
        Button addTask = findViewById(R.id.button3);

        addTask.setOnClickListener(v -> {
            Log.i(TAG, "clicked addTask");

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

            //image view
            imageView =  findViewById(R.id.imageView);

            file = "public/" + UUID.randomUUID().toString();



//                Task newTask = new Task(tasksText, tasksText1, tasksButtons);
//                taskDatabase.taskDao().saveTask(newTask);
            getApplicationContext().startService(new Intent(getApplicationContext(), TransferService.class));

            runTaskMutation(tasksText,tasksText1,tasksButtons, file);
            uploadWithTransferUtility();

            AWSMobileClient.getInstance().initialize(getApplicationContext(), new Callback<UserStateDetails>() {
            @Override
            public void onResult(UserStateDetails userStateDetails) {
                Log.i(TAG, "AWSMobileClient initialized. User State is " + userStateDetails.getUserState());
                uploadWithTransferUtility();
            }
            @Override
            public void onError(Exception e) {
                Log.e(TAG, "Initialization error.", e);
            }
        });
            });

        //upload photos
        Button uploadButton = findViewById(R.id.uploadButton);
        uploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(AddTask.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED){
                    ActivityCompat.requestPermissions(AddTask.this, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
                } else {

                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, OPEN_DOCUMENT_CODE);

                }
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

            }
        }
    }

//    private void imageSelected(Uri uri){
//        imageView.setImageURI(uri);
//
////        uploadWithTransferUtility();
//
//    }

    @Override
    protected void onResume(){
        super.onResume();
//        tasks = taskDatabase.taskDao().getAll();
        TextView allTasksCount = findViewById(R.id.totalTasks);
//        allTasksCount.setText("Total Tasks: " + tasks.size());
        Log.i(TAG, "resumed");
    }

    public void runTaskMutation(String title, String body, String state, String file){
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
//            Intent takeMeBackToMainPage = new Intent(AddTask.this, MainActivity.class);
//            AddTask.this.startActivity(takeMeBackToMainPage);
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

    @Override
        public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
            if(requestCode != 0){
                return;
            }
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){

                Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, OPEN_DOCUMENT_CODE);
            }
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

    public void uploadWithTransferUtility() {
//        String[] filePath = {MediaStore.Images.Media.DATA};
//        Cursor cursor = getContentResolver().query(uri, filePath, null, null, null);
//        cursor.moveToFirst();
//
//        int columnIndex = cursor.getColumnIndex(filePath[0]);
//        String photoPath = cursor.getString(columnIndex);
//        cursor.close();

        //String photoPath contains the path of the selected image
        TransferUtility transferUtility =
                TransferUtility.builder()
                        .context(getApplicationContext())
                        .awsConfiguration(AWSMobileClient.getInstance().getConfiguration())
                        .s3Client(new AmazonS3Client(AWSMobileClient.getInstance()))
                        .build();

        String s3File = convertUriToFilePath(uri);
        File imageFile = new File(s3File);
        Log.d(TAG, "uploading image to s3 " + s3File);

        TransferObserver uploadObserver =
                transferUtility.upload(file, imageFile);

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
                Log.i(TAG, "Image diod not save into s3");
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



