package com.example.be.tasktracker;

import android.content.Context;
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

import com.example.be.tasktracker.DataModel.Project;

import com.example.be.tasktracker.DataModel.Task;
import com.google.gson.Gson;

import java.util.HashMap;


public class StopwatchFragment extends Fragment {
    TextView runningSubtaskTv;
    TextView timeTv;
    TextView[] listItems;
    Project project;
    Button controlBtn;
    private int CLICKED_COLOR = Color.BLUE;
    private int ORIGINAL_COLOR = Color.CYAN;
    boolean working = false;
    private int workingSubtask;
    Task mTask;
    Thread stopwatchThread;
    //private OnFragmentInteractionListener mListener;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, final ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mTask = new Gson().fromJson(getArguments().getString("KEY"), Task.class);
        if(mTask.getSubtasks()==null)
            System.out.println("FUck Null");
        else
            System.out.println("FUCK SIZE "+mTask.getSubtasks().size()+" "+mTask.getSubtasks().keySet());
        project = mTask.getProject();
        listItems = new TextView[project.getSubtasks().size()];
        View rootView = inflater.inflate(R.layout.fragment_stopwatch, container, false);
        runningSubtaskTv = (TextView) rootView.findViewById(R.id.working_subtask);
        timeTv = (TextView) rootView.findViewById(R.id.stopwatch);
        controlBtn = (Button) rootView.findViewById(R.id.start_done);
        initiateLinearList(rootView);
        //stopwatchThread=new Thread(new StopWatch());
        controlBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!working) {
                    controlBtn.setText("Stop");
                    working = true;
                    stopwatchThread=new Thread(new StopWatch());
                    stopwatchThread.start();

                } else {
                    controlBtn.setText("Start");
                    working = false;
                   // stopwatchThread.stop();
                }
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
            listItems[i].setText(project.getSubtasks().get(i));
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
    class TextViewListener implements View.OnClickListener {
        public TextView clickedView = listItems[0];

        @Override
        public void onClick(View v) {
            if (clickedView == v)
                return;
            for (int i = 0; i < listItems.length; i++) {
                if (listItems[i] == v)
                    workingSubtask = i;
            }

            clickedView.setBackgroundColor(ORIGINAL_COLOR);
            clickedView = (TextView) v;
            v.setBackgroundColor(CLICKED_COLOR);
            runningSubtaskTv.setText(clickedView.getText().toString());
            int seconds =(mTask.getSubtasks().get(project.getSubtasks().get(workingSubtask))).intValue();
            timeTv.setText(convertSecsToText(seconds));
            if (working) {
                controlBtn.setText("Start");
                working = false;
            }
        }
    }


   class StopWatch implements Runnable{
      public int seconds =(mTask.getSubtasks().get(project.getSubtasks().get(workingSubtask))).intValue();

       @Override
       public void run() {
           try {

               while (working) {
                   Thread.sleep(1000);
                   seconds++;
                   getActivity().runOnUiThread(new Runnable() {
                       @Override
                       public void run() {
                           if(working)
                           timeTv.setText(convertSecsToText(seconds));
                       }
                   });
                   if(!working)
                       seconds--;
               }
               //mTask.getSubtasks().put(project.getSubtasks().get(workingSubtask), new Long(seconds));
           } catch (InterruptedException e) {
               e.printStackTrace();
           }
           finally {
               mTask.getSubtasks().put(project.getSubtasks().get(workingSubtask),new Long(seconds));
           }
       }
   }
    public static String convertSecsToText(long seconds){
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
         String newText=sb.toString();
        return newText;
    }
}
