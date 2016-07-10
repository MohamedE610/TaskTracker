package com.example.be.tasktracker;

import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.be.tasktracker.DataModel.Project;
import com.google.gson.Gson;

public class NewTaskActivity extends AppCompatActivity implements ChooseProject.OnTaskStartListener {
    final static String TASK_KEY = "KEY";
    private static final String STOPWATCH_TAG ="STOPWATCH_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);
        if (findViewById(R.id.activity_new_task) != null) {
            if (savedInstanceState != null) {
                return;
            }
            getSupportFragmentManager().beginTransaction().add(R.id.activity_new_task, new ChooseProject()).addToBackStack(null).commit();

        }
    }

    @Override
    public void onBackPressed() {
        if(getSupportFragmentManager().findFragmentByTag(STOPWATCH_TAG)!=null){
            ((OnBackStackPressedListener)getSupportFragmentManager().findFragmentByTag(STOPWATCH_TAG)).onBackPressed();
        }
        super.onBackPressed();

    }

    @Override
    public void onTaskStarted(Project project, String s) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        StopwatchFragment stopwatch = new StopwatchFragment();
        Bundle bundle = new Bundle();
        bundle.putString(TASK_KEY, new Gson().toJson(project));
        bundle.putString("TITLE", s);
        stopwatch.setArguments(bundle);
        fragmentTransaction.replace(R.id.activity_new_task, stopwatch,STOPWATCH_TAG).addToBackStack(null).commit();
    }
}

