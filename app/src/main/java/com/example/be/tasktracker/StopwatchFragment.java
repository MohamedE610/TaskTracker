package com.example.be.tasktracker;

import android.app.Activity;
import android.content.Context;
import android.database.Observable;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.Space;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.be.tasktracker.DataModel.HandleData;
import com.example.be.tasktracker.DataModel.Project;

import com.example.be.tasktracker.DataModel.Task;
import com.google.gson.Gson;

import java.io.File;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;

public class StopwatchFragment extends Fragment {
    TextView runningSubtaskTv;
    TextView timeTv;
    TextView[] listItems;
    Project project;
    ArrayList<String> subtasks;
    ImageView controlBtn;
    ImageView saveBtn;
    boolean working = false;
    private int workingSubtask;
    Task mTask;
    Thread stopwatchThread= new Thread(new RunnableStopWatch());
    private long mSeconds = 0;
    private WorkingBoolean workingBoolean=new WorkingBoolean();
    private TextView sessionTitle;
    //private OnFragmentInteractionListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        project=new Gson().fromJson(getArguments().getString("KEY"),Project.class);

        mTask = new Task(project);
        if(getArguments().get("TITLE")!=null){
            mTask.setTitle(getArguments().get("TITLE").toString());
        }
       // project = mTask.getProject();
        subtasks=project.getSubtasks();
        listItems = new TextView[subtasks.size()];
        View rootView = inflater.inflate(R.layout.fragment_stopwatch, container, false);
        saveBtn=(ImageView) rootView.findViewById(R.id.saveSession);
        runningSubtaskTv = (TextView) rootView.findViewById(R.id.working_subtask);
        timeTv = (TextView) rootView.findViewById(R.id.stopwatch);
        controlBtn = (ImageView) rootView.findViewById(R.id.start_done);
        sessionTitle=(TextView)rootView.findViewById(R.id.sessionTitle);
        if(mTask.getTitle()!=null&&mTask.getTitle().length()>0)
            sessionTitle.setText(mTask.getTitle());
        initiateLinearList(rootView);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(HandleData.saveSession(mTask,new File(getActivity().getFilesDir(),project.getProjectName()))){
                    Toast.makeText(getActivity().getApplicationContext(), "Saved", Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(getActivity().getApplicationContext(), "Error in Saving", Toast.LENGTH_SHORT).show();
                }
            }
        });
        controlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                workingBoolean.setValue(!workingBoolean.isValue());
            }
        });
        hideKeyboard(getActivity());
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        return rootView;
    }

    private void initiateLinearList(View rootView) {
        Space space= (Space) rootView.findViewById(R.id.divider);
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
            } else{
                listItems[i].setSelected(false);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    listItems[i].setTextColor(getResources().getColor(R.color.backgroundColor,null));
                }
                else {
                    listItems[i].setTextColor(getResources().getColor(R.color.backgroundColor));
                }
            }
            listItems[i].setOnClickListener(texViewListener);
            linearLayout.addView(listItems[i]);
            Space space1=new Space(getActivity());
            space1.setLayoutParams(space.getLayoutParams());
            space1.setVisibility(View.VISIBLE);
            linearLayout.addView(space1);

        }
    }

    /* @Override
       public void onAttach(Context context) {
           super.onAttach(context);
           if (context instanceof OnFragmentInteractionListener) {
               mListener = (OnFragmentInteractionListener) context;
           } else {
               throw new RuntimeException(context.toString()
                       + " must implement OnFragmentInteractionListener");
           }
       }

       @Override
       public void onDetach() {
           super.onDetach();
           mListener = null;
       }

   */
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
    class RunnableStopWatch implements Runnable {
        @Override
        public void run() {
            try {
                while (workingBoolean.isValue()) {
                    Thread.sleep(1000);
                    if (workingBoolean.isValue())
                        mSeconds++;
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (workingBoolean.isValue())
                                timeTv.setText(convertSecsToText(mSeconds));
                        }
                    });

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

        public void setValue(boolean value) {
            this.value = value;

            stopwatchThread.interrupt();
            if (value) {
                controlBtn.setActivated(true);
                mSeconds = mTask.getSubtasks().get(subtasks.get(workingSubtask));
                stopwatchThread=new Thread(new RunnableStopWatch());
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
                workingBoolean.setValue(false);
            for (int i = 0; i < listItems.length; i++) {
                if (listItems[i] == v) {
                    workingSubtask = i;
                    break;
                }
            }

            clickedView.setSelected(false);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                clickedView.setTextColor(getResources().getColor(R.color.backgroundColor,null));
                ((TextView)v).setTextColor(getResources().getColor(R.color.colorWhite,null));
            }
            else {
                clickedView.setTextColor(getResources().getColor(R.color.backgroundColor));
                ((TextView)v).setTextColor(getResources().getColor(R.color.colorWhite));
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
