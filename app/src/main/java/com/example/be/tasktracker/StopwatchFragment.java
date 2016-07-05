package com.example.be.tasktracker;

import android.content.Context;
import android.database.Observable;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

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
    Button controlBtn;
    Button saveBtn;
    private int CLICKED_COLOR = Color.BLUE;
    private int ORIGINAL_COLOR = Color.CYAN;
    boolean working = false;
    private int workingSubtask;
    Task mTask;
    Thread stopwatchThread= new Thread(new RunnableStopWatch());
    private long mSeconds = 0;
    private WorkingBoolean workingBoolean=new WorkingBoolean();
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
        saveBtn=(Button)rootView.findViewById(R.id.saveSession);
        runningSubtaskTv = (TextView) rootView.findViewById(R.id.working_subtask);
        timeTv = (TextView) rootView.findViewById(R.id.stopwatch);
        controlBtn = (Button) rootView.findViewById(R.id.start_done);
        initiateLinearList(rootView);
        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                HandleData.saveSession(mTask,new File(getActivity().getFilesDir(),project.getProjectName()));
            }
        });
        controlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                workingBoolean.setValue(!workingBoolean.isValue());
            }
        });

        return rootView;
    }

    private void initiateLinearList(View rootView) {
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
            //listItems[i].setPadding(0, 0, 20, 0);
            listItems[i].setClickable(true);

            if (i == 0) {
                listItems[i].setBackgroundColor(CLICKED_COLOR);
                texViewListener = new TextViewListener();
                runningSubtaskTv.setText(listItems[i].getText().toString());
            } else
                listItems[i].setBackgroundColor(ORIGINAL_COLOR);
            listItems[i].setOnClickListener(texViewListener);
            linearLayout.addView(listItems[i]);

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
                mSeconds = mTask.getSubtasks().get(subtasks.get(workingSubtask));
                controlBtn.setText("Stop");
                stopwatchThread=new Thread(new RunnableStopWatch());
                stopwatchThread.start();

            } else {
                controlBtn.setText("Start");
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

            clickedView.setBackgroundColor(ORIGINAL_COLOR);
            clickedView = (TextView) v;
            v.setBackgroundColor(CLICKED_COLOR);
            runningSubtaskTv.setText(clickedView.getText().toString());
            timeTv.setText(convertSecsToText(mTask.getSubtasks().get(subtasks.get(workingSubtask))));

        }
    }


}
