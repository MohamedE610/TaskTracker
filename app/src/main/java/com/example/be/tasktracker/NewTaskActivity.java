package com.example.be.tasktracker;

import android.content.Intent;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.be.tasktracker.DataModel.Project;
import com.example.be.tasktracker.Interfaces.OnBackStackPressedListener;
import com.google.gson.Gson;

public class NewTaskActivity extends AppCompatActivity implements ChooseProject.OnTaskStartListener {
    final static String Session_KEY = "KEY";
    private static final String STOPWATCH_TAG = "STOPWATCH_TAG";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);
        if (findViewById(R.id.activity_new_task) != null) {
            if (savedInstanceState != null || getSupportFragmentManager().findFragmentByTag(STOPWATCH_TAG) != null) {
                return;
            }
            getSupportFragmentManager().beginTransaction().add(R.id.activity_new_task, new ChooseProject()).commit();

        }
    }

    @Override
    public void onBackPressed() {

        if (getSupportFragmentManager().findFragmentByTag(STOPWATCH_TAG) != null) {
            if (((StopwatchFragment) getSupportFragmentManager().findFragmentByTag(STOPWATCH_TAG)).getSessionController().isWorking()) {
                Intent startMain = new Intent(Intent.ACTION_MAIN);
                startMain.addCategory(Intent.CATEGORY_HOME);
                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(startMain);

            } else if (((OnBackStackPressedListener) getSupportFragmentManager().findFragmentByTag(STOPWATCH_TAG)).onBackPressed()){
                SessionController.destroy();
                super.onBackPressed();

            }

        } else{
            SessionController.destroy();
            super.onBackPressed();
        }

    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("NewTaskActiviy","onDestroyActivity");
    }
    @Override
    public void onTaskStarted(Project project, String s) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        StopwatchFragment stopwatch = new StopwatchFragment();
        Bundle bundle = new Bundle();
        bundle.putString(Session_KEY, new Gson().toJson(project));
        bundle.putString("TITLE", s);
        stopwatch.setArguments(bundle);
        fragmentTransaction.replace(R.id.activity_new_task, stopwatch, STOPWATCH_TAG).addToBackStack(null).commit();
    }
}

