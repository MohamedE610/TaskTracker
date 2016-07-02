package com.example.be.tasktracker;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class NewTaskActivity extends AppCompatActivity implements  ChooseProject.OnTaskStartListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);
    }

    @Override
    public void onTaskStarted(String projectTitle) {

    }
}

