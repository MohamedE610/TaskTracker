package com.example.be.tasktracker;

import android.app.Activity;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.widget.Space;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.be.tasktracker.DataModel.DataHandler;
import com.example.be.tasktracker.DataModel.Project;
import com.example.be.tasktracker.DataModel.Session;
import com.example.be.tasktracker.Interfaces.OnBackStackPressedListener;
import com.example.be.tasktracker.Interfaces.StopwatchObserver;
import com.example.be.tasktracker.Services.NotificationService;
import com.google.gson.Gson;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.File;
import java.util.ArrayList;
import java.util.TimeZone;

/**
 * Created by BE on 7/25/2016.
 */
public class StopwatchFragment extends Fragment implements OnBackStackPressedListener,StopwatchObserver {
    SessionController mSessionController;
    TextView runningTaskTv;
    NotificationManager mNotificationManager;
    TextView timeTv;
    TextView[] listItems;
    Project project;
    ArrayList<String> tasks;
    ImageView controlBtn;
    Bundle savedBundle;
    ImageView saveBtn;
    Gson gson = new Gson();
    private TextView sessionTitle;
    private boolean activityRecreated;
    TextView dateTV;
    boolean cancel;

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        System.out.println("OnActivityCreated");
        savedBundle = savedInstanceState;
        activityRecreated = true; //commented
    }

    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!activityRecreated) {   //commented
            savedBundle = savedInstanceState; //commented
            activityRecreated = true;  //commented
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //   setRetainInstance(true);
        project = gson.fromJson(getArguments().getString("KEY"), Project.class);
        if (mSessionController == null) {
            mSessionController = SessionController.getInstance(new Session(project));
        }

        if (getArguments().get("TITLE") != null) {
            mSessionController.getmSession().setTitle(getArguments().get("TITLE").toString());
        }
        tasks = project.getTasks();
        listItems = new TextView[tasks.size()];
        View rootView = inflater.inflate(R.layout.fragment_stopwatch, container, false);
        saveBtn = (ImageView) rootView.findViewById(R.id.saveSession);
        runningTaskTv = (TextView) rootView.findViewById(R.id.working_subtask);
        timeTv = (TextView) rootView.findViewById(R.id.stopwatch);
        controlBtn = (ImageView) rootView.findViewById(R.id.start_done);
        sessionTitle = (TextView) rootView.findViewById(R.id.sessionTitle);
        dateTV = (TextView) rootView.findViewById(R.id.sessionDate);
        dateTV.setText(getDateString(mSessionController.getmSession().getDateInMs()));
        if (mSessionController.getmSession().getTitle() != null && mSessionController.getmSession().getTitle().length() > 0)
            sessionTitle.setText(mSessionController.getmSession().getTitle());
        initiateLinearList(rootView);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DataHandler.saveSession(mSessionController.getmSession(), new File(getActivity().getFilesDir(), project.getProjectName()))) {
                    Toast.makeText(getActivity().getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();
                    mSessionController.setSavedState(SessionController.SaveState.SAVED);
                    if (mSessionController.isWorking())
                        mSessionController.setWorking(false);
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "Error in Saving", Toast.LENGTH_SHORT).show();
                }
            }
        });
        controlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSessionController.setWorking((!mSessionController.isWorking()));
            }
        });
        hideKeyboard(getActivity());
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mNotificationManager =
                (NotificationManager) getActivity().getSystemService(Context.NOTIFICATION_SERVICE);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        mSessionController.registerObserver(this);
        if (mSessionController.isWorking()) {
            controlBtn.setActivated(true);
        }
        activityRecreated = false;  //commented
    }
    @Override
    public void onStop() {
        super.onStop();
        mSessionController.unRegisterObserver(this);
    }
    @Override
    public void onSaveInstanceState(Bundle outState) {
        System.out.println("Called onSaveInstanceState");
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("StopWatchFragment", "onDestroyFragment");
    }

    @Override
    public boolean onBackPressed() {
        //if the session isn't saved/updated show confirmation dialog
        switch (mSessionController.getSavedState()) {
            case NOT_SAVED:
                showDialog("Do you want to save this session ? ");
                break;
            case UPDATES_NOT_SAVED:
                showDialog("Changes has been made to this session .. Do you want to save the changes ? ");
                break;

        }
        //as this value is returned without waiting the result from dialog
        return mSessionController.getSavedState()== SessionController.SaveState.SAVED || mSessionController.getSavedState() == SessionController.SaveState.ABORT_SAVING;

    }

    private void initiateLinearList(View rootView) {
        Space space = (Space) rootView.findViewById(R.id.divider);
        space.setVisibility(View.INVISIBLE);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        int height = displaymetrics.heightPixels;
        int width = displaymetrics.widthPixels;
        float scale = getResources().getDisplayMetrics().density;
        int dpAsPixelsHeight = (int) (50 * scale + 0.5f);
        int dpAsPixelsWidth = (int) (140 * scale + 0.5f);
        LinearLayout linearLayout = (LinearLayout) rootView.findViewById(R.id.horiozontal_list);
        TextViewListener texViewListener = null;
        for (int i = 0; i < listItems.length; i++) {
            listItems[i] = new TextView(getActivity());
            listItems[i].setText(tasks.get(i));
            if (140 * listItems.length < width)
                listItems[i].setWidth(width / listItems.length);
            else
                listItems[i].setWidth(dpAsPixelsWidth);
            listItems[i].setGravity(Gravity.CENTER);
            listItems[i].setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16);
            listItems[i].setMinHeight(dpAsPixelsHeight);
            listItems[i].setClickable(true);
            listItems[i].setBackgroundResource(R.drawable.tvselector);


            if (i == mSessionController.getWorkingTask()) {
                listItems[i].setBackgroundResource(R.drawable.tvselector);
                listItems[i].setSelected(true);
                texViewListener = new TextViewListener();
                runningTaskTv.setText(listItems[i].getText().toString());
            } else {
                listItems[i].setSelected(false);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    listItems[i].setTextColor(getResources().getColor(R.color.backgroundColor, null));
                } else {
                    listItems[i].setTextColor(getResources().getColor(R.color.backgroundColor));
                }
            }
            listItems[i].setOnClickListener(texViewListener);
            linearLayout.addView(listItems[i]);
            Space space1 = new Space(getActivity());
            space1.setLayoutParams(space.getLayoutParams());
            space1.setVisibility(View.VISIBLE);
            linearLayout.addView(space1);

        }
    }

    public SessionController getSessionController() {
        return mSessionController;
    }

    private void showDialog(String s) {
        cancel = false;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Save Changes");
        builder.setMessage(s);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (DataHandler.saveSession(mSessionController.getmSession(), new File(getActivity().getFilesDir(), project.getProjectName()))) {
                    Toast.makeText(getActivity().getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();
                    mSessionController.setSavedState(SessionController.SaveState.SAVED);
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "Error in Saving", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mSessionController.setSavedState(SessionController.SaveState.ABORT_SAVING);
            }
        });

        builder.setCancelable(true);
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                cancel = true;
            }
        });

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {


            @Override
            public void onDismiss(DialogInterface dialog) {
                //so that we can keep dialog msg before fragment destroyed
                // if cancel is clicked don't call onBack again and super.onBackPressed() won't
                //be called ( frankly don't execute back )
                if (!cancel) {
                    getActivity().onBackPressed();
                }
            }
        });

        builder.show();
    }



    class TextViewListener implements View.OnClickListener {
        public TextView clickedView = listItems[0];

        @Override
        public void onClick(View v) {
            if (clickedView == v)
                return;

            for (int i = 0; i < listItems.length; i++) {
                if (listItems[i] == v) {
                    mSessionController.setWorkingTask(i);
                    break;
                }
            }

            clickedView.setSelected(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                clickedView.setTextColor(getResources().getColor(R.color.backgroundColor, null));
                ((TextView) v).setTextColor(getResources().getColor(R.color.colorWhite, null));
            } else {
                clickedView.setTextColor(getResources().getColor(R.color.backgroundColor));
                ((TextView) v).setTextColor(getResources().getColor(R.color.colorWhite));
            }
            clickedView = (TextView) v;
            clickedView.setSelected(true);
            runningTaskTv.setText(clickedView.getText().toString());
            timeTv.setText(convertSecsToText(mSessionController.getmSession().getTasks().get(mSessionController.getWorkingTaskName())));

        }

    }
    public static void hideKeyboard(Context ctx) {
        InputMethodManager inputManager = (InputMethodManager) ctx
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        // check if no view has focus:
        View v = ((Activity) ctx).getCurrentFocus();
        if (v == null)
            return;

        inputManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    public static String getDateString(Long dateInMs) {
        return (new DateTime(dateInMs, DateTimeZone.forTimeZone(TimeZone.getDefault())))
                .toString("d/M/Y  H:m:s");

    }


    public static String convertSecsToText(long seconds) {
        StringBuilder sb = new StringBuilder();
        int min = (int) (seconds / 60);
        int secs = (int) (seconds % 60);
        if (min < 10)
            sb.append("0" + min);
        else
            sb.append(min);
        sb.append(":");
        if (secs < 10)
            sb.append("0" + secs);
        else
            sb.append(secs);
        String newText = sb.toString();
        return newText;
    }


    @Override
    public void onSecondsIncreased(int second) {
        updateTimeTextView();
    }



    @Override
    public void onStopwatchStateChanged(boolean working) {
        if(working){
            controlBtn.setActivated(true);
            updateTimeTextView();
            //startService
            Intent startIntent = new Intent(getActivity(), NotificationService.class);
            startIntent.setAction(NotificationService.ServiceAction_START);
            getActivity().startService(startIntent);
        }
        else{
            controlBtn.setActivated(false);
          //  updateTimeTextView();
            Intent startIntent = new Intent(getActivity(), NotificationService.class);
            startIntent.setAction(NotificationService.ServiceAction_STOP_HIDE);
            getActivity().startService(startIntent);
            //stopService
        }
    }
    private void updateTimeTextView() {
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                timeTv.setText(convertSecsToText(mSessionController.getmSeconds()));
            }
        });
    }
}
