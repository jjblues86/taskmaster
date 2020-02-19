package com.example.taskmaster;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

public class TaskDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_detail);

        String textClicked = getIntent().getStringExtra("mNamedView");
        String body = getIntent().getStringExtra("mBodyView");
        String state = getIntent().getStringExtra("mStateView");

        TextView buttonClickedTextView = findViewById(R.id.textView3);
        TextView stateClicked = findViewById(R.id.textView5);
        TextView bodyClicked = findViewById(R.id.textView8);
        buttonClickedTextView.setText(textClicked);
        stateClicked.setText(state);
        bodyClicked.setText(body);


    }
}
