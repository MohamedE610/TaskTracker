package com.example.be.tasktracker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import com.example.be.tasktracker.DataModel.Project;
import com.example.be.tasktracker.Services.NotificationService;
import com.google.gson.Gson;

public class NewTaskActivity extends AppCompatActivity implements ChooseProject.OnTaskStartListener {
    final static String PROJECT_KEY = "KEY";
    private static final String STOPWATCH_TAG = "STOPWATCH_TAG";
    public static final String RESUME_STOPWATCH = "RESUME_STOPWATCH";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_task);

        if (findViewById(R.id.activity_new_task) != null) {
            if (getIntent() != null && getIntent().getAction() == RESUME_STOPWATCH) {
                if (SessionController.exist) {
                    SessionController sessionController = SessionController.getInstance(null);
                    getSupportFragmentManager().beginTransaction().add
                            (R.id.activity_new_task,
                                    produceStopwatchFragment(sessionController.getProject(), sessionController.getmSession().getTitle())
                                    , STOPWATCH_TAG).commit();
                    return;
                } else {
                    Intent stopIntent = new Intent(this, NotificationService.class);
                    stopIntent.setAction(NotificationService.ServiceAction_STOP_HIDE);
                    startService(stopIntent);
                }

            }
            if (savedInstanceState != null || getSupportFragmentManager().findFragmentByTag(STOPWATCH_TAG) != null) {
                return;
            }
            getSupportFragmentManager().beginTransaction().add(R.id.activity_new_task, new ChooseProject()).commit();

        }
    }

    @Override
    public void onBackPressed() {
        boolean goBack = false;
        if (getSupportFragmentManager().findFragmentByTag(STOPWATCH_TAG) != null) {
            StopwatchFragment stopwatchFragment = (StopwatchFragment) getSupportFragmentManager().findFragmentByTag(STOPWATCH_TAG);
            if (stopwatchFragment.getSessionController().isWorking()) {
                Intent startMain = new Intent(Intent.ACTION_MAIN);
                startMain.addCategory(Intent.CATEGORY_HOME);
                startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(startMain);

            } else if (stopwatchFragment.getSessionController().getSavedState() == SessionController.SaveState.NEW)
                goBack = true;
            else if (stopwatchFragment.onBackPressed())
                goBack = true;
        } else
            goBack = true;

        if (goBack) {
            SessionController.destroy();
            super.onBackPressed();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("NewTaskActiviy", "onDestroyActivity");
    }

    @Override
    public void onTaskStarted(Project project, String s) {
        getSupportFragmentManager().beginTransaction().replace(R.id.activity_new_task, produceStopwatchFragment(project, s), STOPWATCH_TAG).addToBackStack(null).commit();
    }

    public StopwatchFragment produceStopwatchFragment(Project project, String s) {

        StopwatchFragment stopwatch = new StopwatchFragment();
        Bundle bundle = new Bundle();
        bundle.putString(PROJECT_KEY, new Gson().toJson(project));
        bundle.putString("TITLE", s);
        stopwatch.setArguments(bundle);
        return stopwatch;
    }
}

