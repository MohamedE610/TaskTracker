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
import android.support.v4.widget.Space;
import android.support.v7.app.AlertDialog;
import android.support.v4.app.NotificationCompat;
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
import com.google.gson.Gson;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.File;
import java.util.ArrayList;
import java.util.TimeZone;

public class StopwatchFragment extends Fragment implements OnBackStackPressedListener {
    private static final String SESSION_KEY = "SESSION";
    private static final String WORKING_KEY = "WORKING";
    private static final String ELAPSED_SECONDS = "ELAPSED";
    private static final String OFFTIME_MS = "OFFTIME";
    private static final String WORKING_TASK = "WORKING_TASK";

    enum SaveState {SAVED, UPDATES_NOT_SAVED, NOT_SAVED, ABORT_SAVING}

    final int NOTIFY_ID = 1;
    SaveState saveState = SaveState.NOT_SAVED;
    TextView runningTaskTv;
    NotificationManager mNotificationManager;
    TextView timeTv;
    TextView[] listItems;
    Project project;
    ArrayList<String> tasks;
    ImageView controlBtn;
    Bundle savedBundle;
    ImageView saveBtn;
    NotificationThread notificationThread;
    private int workingTask;
    Session mSession;
    Gson gson = new Gson();
    long sleeping = 0;
    Thread stopwatchThread = new Thread(new RunnableStopWatch());
    private long mSeconds = 0;


    private WorkingBoolean workingBoolean = new WorkingBoolean();
    private TextView sessionTitle;
    private boolean activityRecreated;
    TextView dateTV;
    boolean cancel;
    //private OnFragmentInteractionListener mListener;


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        System.out.println("OnActivityCreated");
        savedBundle = savedInstanceState;
        activityRecreated = true;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!activityRecreated) {
            savedBundle = savedInstanceState;
            activityRecreated = true;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //   setRetainInstance(true);
        project = gson.fromJson(getArguments().getString("KEY"), Project.class);
        if (mSession == null)
            mSession = new Session(project);

        if (getArguments().get("TITLE") != null) {
            mSession.setTitle(getArguments().get("TITLE").toString());
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
        dateTV.setText(getDateString(mSession.getDateInMs()));
        if (mSession.getTitle() != null && mSession.getTitle().length() > 0)
            sessionTitle.setText(mSession.getTitle());
        initiateLinearList(rootView);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (DataHandler.saveSession(mSession, new File(getActivity().getFilesDir(), project.getProjectName()))) {
                    Toast.makeText(getActivity().getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();
                    saveState = SaveState.SAVED;
                    if (workingBoolean.isValue())
                        workingBoolean.setValue(false, true);
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "Error in Saving", Toast.LENGTH_SHORT).show();
                }
            }
        });
        controlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                workingBoolean.setValue(!workingBoolean.isValue(), false);
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
        if (workingBoolean.isValue()) {
            if (notificationThread != null && notificationThread.isAlive()) {
                mSeconds = notificationThread.getSeconds();
            } else
                mSeconds += ((System.nanoTime() - sleeping) / 1000000000.0) + 1;
            timeTv.setText(convertSecsToText(mSeconds));
            workingBoolean.setValue(true, true);
        } else if (activityRecreated && savedBundle != null && (savedBundle.getString(SESSION_KEY) != null)) {
            mSession = gson.fromJson(savedBundle.getString(SESSION_KEY), Session.class);
            mSeconds = savedBundle.getLong(ELAPSED_SECONDS);
            if (savedBundle.getBoolean(WORKING_KEY)) {
                sleeping = savedBundle.getLong(OFFTIME_MS);
                mSeconds += ((System.nanoTime() - sleeping) / 1000000000.0) + 1;
                timeTv.setText(convertSecsToText(mSeconds));
                workingTask = savedBundle.getInt(WORKING_TASK);
                workingBoolean.setValue(true, true);

            }

        }
        activityRecreated = false;
    }

    @Override
    public void onStop() {
        super.onStop();
        //if counter is running stop the StopwatchThread and start notification Background Thread
        if (workingBoolean.isValue()) {
            stopwatchThread.interrupt();
            mSession.getTasks().put(tasks.get(workingTask), mSeconds);
            getNotifictaionThread();
            notificationThread.start();

        }
    }


    @Override
    public void onSaveInstanceState(Bundle outState) {
        System.out.println("Called onSaveInstanceState");

        sleeping = System.nanoTime();
        outState.putString(SESSION_KEY, gson.toJson(mSession));
        outState.putBoolean(WORKING_KEY, workingBoolean.isValue());
        outState.putLong(ELAPSED_SECONDS, mSeconds);
        outState.putLong(OFFTIME_MS, sleeping);
        outState.putInt(WORKING_TASK, workingTask);
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
        switch (saveState) {
            case NOT_SAVED:
                showDialog("Do you want to save this session ? ");
                break;
            case UPDATES_NOT_SAVED:
                showDialog("Changes has been made to this session .. Do you want to save the changes ? ");
                break;

        }
        //as this value is returned without waiting the result from dialog
        return saveState == SaveState.SAVED || saveState == SaveState.ABORT_SAVING;

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


            if (i == 0) {
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

    public WorkingBoolean getWorkingBoolean() {
        return workingBoolean;
    }

    private NotificationCompat.Builder getBuilder() {
        Intent notificationIntent = new Intent(getActivity(), MainActivity.class);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        PendingIntent pendingIntent = PendingIntent.getActivity(getActivity(), 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);


      //  PendingIntent pendingIntent=PendingIntent.getActivity(getActivity(),1,new Intent(getActivity(),NewTaskActivity.class),PendingIntent.FLAG_CANCEL_CURRENT);
        return new NotificationCompat.Builder(getActivity())
                .setContentTitle(tasks.get(workingTask))
                .setContentText(convertSecsToText(mSeconds))
                .addAction(R.drawable.arrow,"Next",null)
                .addAction(R.drawable.stopbtn,"Stop",null)
                .setSmallIcon(R.drawable.icon).setContentIntent(pendingIntent);
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


    private void showDialog(String s) {
        cancel = false;
        if (workingBoolean.isValue())
            workingBoolean.setValue(false, false);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Save Changes");
        builder.setMessage(s);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (DataHandler.saveSession(mSession, new File(getActivity().getFilesDir(), project.getProjectName()))) {
                    Toast.makeText(getActivity().getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();
                    saveState = SaveState.SAVED;
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "Error in Saving", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                saveState = SaveState.ABORT_SAVING;
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

    class RunnableStopWatch implements Runnable {
        NotificationCompat.Builder mNotifyBuilder;

        @Override
        public void run() {
            mNotifyBuilder = getBuilder();
            try {
                while (workingBoolean.isValue()) {
                    Thread.sleep(1000);

                    if (workingBoolean.isValue())
                        mSeconds++;

                    mNotifyBuilder.setContentText(convertSecsToText(mSeconds));
                    mNotificationManager.notify(
                            NOTIFY_ID,
                            mNotifyBuilder.build());
                    System.out.println("time elapse:: " + mSeconds);
                    try {
                        getActivity().runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (workingBoolean.isValue())
                                    timeTv.setText(convertSecsToText(mSeconds));
                            }
                        });
                    } catch (NullPointerException ex) {
                        ex.printStackTrace();
                    }

                }
            } catch (InterruptedException e) {

            }
        }
    }

    class WorkingBoolean {
        boolean value = false;

        public boolean isValue() {
            return value;
        }

        public void setValue(boolean value, boolean synchronizeSeconds) {
            this.value = value;

            if (value) {
                saveState = (saveState == SaveState.SAVED ? SaveState.UPDATES_NOT_SAVED : SaveState.SAVED.NOT_SAVED);
                controlBtn.setActivated(true);
                //synchronize mSeconds with the elapsed time and don't read them from saved session
                if (!synchronizeSeconds)
                    mSeconds = mSession.getTasks().get(tasks.get(workingTask));
                stopwatchThread = new Thread(new RunnableStopWatch());
                getNotifictaionThread();
                if(notificationThread.isAlive()){
                    mSeconds=notificationThread.getSeconds();
                    notificationThread.interrupt();
                }
                stopwatchThread.start();


            } else {
                controlBtn.setActivated(false);
                mSession.getTasks().put(tasks.get(workingTask), mSeconds);
                stopwatchThread.interrupt();
                //notificationThread.interrupt();
                mNotificationManager.cancel(NOTIFY_ID);
            }

        }
    }

    private boolean getNotifictaionThread() {
        if (notificationThread == null) {
            for (Thread thread : Thread.getAllStackTraces().keySet()) {
                if (thread instanceof NotificationThread) {
                    notificationThread = (NotificationThread) thread;
                    return true;
                }
            }
        }

        if (notificationThread == null || !notificationThread.isAlive()) {
            notificationThread = new NotificationThread(mSeconds);
            notificationThread.setName("NotificationThread");
        }
        return false;
    }

    class TextViewListener implements View.OnClickListener {
        public TextView clickedView = listItems[0];

        @Override
        public void onClick(View v) {
            if (clickedView == v)
                return;
            if (workingBoolean.isValue())
                workingBoolean.setValue(false, false);
            for (int i = 0; i < listItems.length; i++) {
                if (listItems[i] == v) {
                    workingTask = i;
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
            timeTv.setText(convertSecsToText(mSession.getTasks().get(tasks.get(workingTask))));

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


    private class NotificationThread extends Thread {
        long seconds;
        NotificationCompat.Builder mNotifyBuilder;

        NotificationThread(long seconds) {
            super();
            this.seconds = seconds;
            mNotifyBuilder = getBuilder();
        }

        @Override
        public void run() {

            try {

                while (true) {
                    Thread.sleep(1000);
                    mNotifyBuilder.setContentText(convertSecsToText(++seconds));
                    mNotificationManager.notify(
                            NOTIFY_ID,
                            mNotifyBuilder.build());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        public long getSeconds() {
            return seconds;
        }
    }
}
