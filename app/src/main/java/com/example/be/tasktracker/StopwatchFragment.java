package com.example.be.tasktracker;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.Space;
import android.support.v7.app.AlertDialog;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.be.tasktracker.DataModel.HandleData;
import com.example.be.tasktracker.DataModel.Project;

import com.example.be.tasktracker.DataModel.Task;
import com.google.gson.Gson;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.File;
import java.util.ArrayList;
import java.util.TimeZone;

public class StopwatchFragment extends Fragment implements OnBackStackPressedListener {
    private static final String TASK_KEY = "TASK";
    private static final String WORKING_KEY = "WORKING";
    private static final String ELAPSED_SECONDS = "ELAPSED";
    private static final String OFFTIME_MS = "OFFTIME";
    private static final String WORKING_SUBTASK = "WORKING_SUBTASK";
    enum SaveState{SAVED,UPDATES_NOT_SAVED,NOT_SAVED,ABORT_SAVING}
    SaveState saveState=SaveState.NOT_SAVED;
    TextView runningSubtaskTv;
    TextView timeTv;
    TextView[] listItems;
    Project project;
    ArrayList<String> subtasks;
    ImageView controlBtn;
    Bundle savedBundle;
    ImageView saveBtn;
    private int workingSubtask;
    Task mTask;
    Gson gson = new Gson();
    long sleeping = 0;
    Thread stopwatchThread = new Thread(new RunnableStopWatch());
    private long mSeconds = 0;
    private WorkingBoolean workingBoolean = new WorkingBoolean();
    private TextView sessionTitle;
    private boolean activityDestroyed;
    TextView dateTV;
    //private OnFragmentInteractionListener mListener;


    @Override
    public void onStart() {
        super.onStart();
        if (workingBoolean.isValue()) {
            mSeconds += ((System.nanoTime() - sleeping) / 1000000000.0)+1;
            timeTv.setText(convertSecsToText(mSeconds));
            workingBoolean.setValue(true, true);
        } else if (activityDestroyed && savedBundle != null && (savedBundle.getString(TASK_KEY) != null)) {
            mTask = gson.fromJson(savedBundle.getString(TASK_KEY), Task.class);
            mSeconds = savedBundle.getLong(ELAPSED_SECONDS);
            if (savedBundle.getBoolean(WORKING_KEY)) {
                sleeping = savedBundle.getLong(OFFTIME_MS);
                mSeconds += ((System.nanoTime() - sleeping) / 1000000000.0)+1;
                timeTv.setText(convertSecsToText(mSeconds));
                workingSubtask = savedBundle.getInt(WORKING_SUBTASK);
                workingBoolean.setValue(true, true);

            }

        }
        activityDestroyed = false;
    }


    @Override
    public void onStop() {
        super.onStop();
        if (workingBoolean.isValue()) {
            stopwatchThread.interrupt();
            mTask.getSubtasks().put(subtasks.get(workingSubtask), mSeconds);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        System.out.println("Called onSaveInstanceState");

        super.onSaveInstanceState(outState);
        sleeping = System.nanoTime();
        outState.putString(TASK_KEY, gson.toJson(mTask));
        outState.putBoolean(WORKING_KEY, workingBoolean.isValue());
        outState.putLong(ELAPSED_SECONDS, mSeconds);
        outState.putLong(OFFTIME_MS, sleeping);
        outState.putInt(WORKING_SUBTASK, workingSubtask);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        setRetainInstance(true);
        project = gson.fromJson(getArguments().getString("KEY"), Project.class);
        if (mTask == null)
            mTask = new Task(project);

        if (getArguments().get("TITLE") != null) {
            mTask.setTitle(getArguments().get("TITLE").toString());
        }
        subtasks = project.getSubtasks();
        listItems = new TextView[subtasks.size()];
        View rootView = inflater.inflate(R.layout.fragment_stopwatch, container, false);
        saveBtn = (ImageView) rootView.findViewById(R.id.saveSession);
        runningSubtaskTv = (TextView) rootView.findViewById(R.id.working_subtask);
        timeTv = (TextView) rootView.findViewById(R.id.stopwatch);
        controlBtn = (ImageView) rootView.findViewById(R.id.start_done);
        sessionTitle = (TextView) rootView.findViewById(R.id.sessionTitle);
        dateTV=(TextView)rootView.findViewById(R.id.sessionDate);
        DateTime dateTime=new DateTime((long)mTask.getDateInMs(),DateTimeZone.forTimeZone(TimeZone.getDefault()));
        dateTV.setText(dateTime.toString("d/M/Y  H:m:s"));
        if (mTask.getTitle() != null && mTask.getTitle().length() > 0)
            sessionTitle.setText(mTask.getTitle());
        initiateLinearList(rootView);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(workingBoolean.isValue()) //if counter is working stop it on saving
                    workingBoolean.setValue(false,false);
                if (HandleData.saveSession(mTask, new File(getActivity().getFilesDir(), project.getProjectName()))) {
                    Toast.makeText(getActivity().getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();
                    saveState=SaveState.SAVED;
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

        return rootView;
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
            listItems[i].setText(subtasks.get(i));
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
                runningSubtaskTv.setText(listItems[i].getText().toString());
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

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        System.out.println("OnActivityCreated");
        savedBundle = savedInstanceState;
        activityDestroyed = true;
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
    public boolean onBackPressed() {
        switch (saveState){
            case NOT_SAVED:
                 showDialog("Do you want to save this session ? ");
                break;
            case UPDATES_NOT_SAVED:
                 showDialog("Changes has been made to this session .. Do you want to save the changes ? ");
                break;

        }
        return saveState==SaveState.SAVED||saveState==SaveState.ABORT_SAVING;

    }


    private void showDialog(String s) {
        if(workingBoolean.isValue())
            workingBoolean.setValue(false,false);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Save Changes");
        builder.setMessage(s);
        builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (HandleData.saveSession(mTask, new File(getActivity().getFilesDir(), project.getProjectName()))) {
                    Toast.makeText(getActivity().getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();
                    saveState=SaveState.SAVED;
                } else {
                    Toast.makeText(getActivity().getApplicationContext(), "Error in Saving", Toast.LENGTH_SHORT).show();
                }
            }
        });
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                 saveState=SaveState.ABORT_SAVING;
            }
        });

        builder.setOnDismissListener(new DialogInterface.OnDismissListener(){


            @Override
            public void onDismiss(DialogInterface dialog) {
                //so that we can keep dialog msg before fragment destroyed
                  getActivity().onBackPressed();
            }
        });
        builder.show();
    }

    class RunnableStopWatch implements Runnable {
        @Override
        public void run() {
            try {
                while (workingBoolean.isValue()) {
                    Thread.sleep(1000);

                    if (workingBoolean.isValue())
                        mSeconds++;
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

        public void setValue(boolean value, boolean override) {
            this.value = value;

            stopwatchThread.interrupt();
            if (value) {
               saveState= (saveState == SaveState.SAVED ? SaveState.UPDATES_NOT_SAVED:SaveState.SAVED.NOT_SAVED);
                controlBtn.setActivated(true);
                if (!override)
                    mSeconds = mTask.getSubtasks().get(subtasks.get(workingSubtask));
                stopwatchThread = new Thread(new RunnableStopWatch());
                stopwatchThread.start();

            } else {
                controlBtn.setActivated(false);
                mTask.getSubtasks().put(subtasks.get(workingSubtask), mSeconds);
                stopwatchThread.interrupt();
            }

        }
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
                    workingSubtask = i;
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
            runningSubtaskTv.setText(clickedView.getText().toString());
            timeTv.setText(convertSecsToText(mTask.getSubtasks().get(subtasks.get(workingSubtask))));

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


}
