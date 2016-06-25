package com.example.be.tasktracker;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

public class MainFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    Button createPatternBtn;
    Button loadPatternBtn;
    Button statisticsBtn;
    OnFragmentInteractionListener mListener;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView= inflater.inflate(R.layout.fragment_main, container, false);
        createPatternBtn=(Button)rootView.findViewById(R.id.create);
        loadPatternBtn=(Button)rootView.findViewById(R.id.load);
        statisticsBtn=(Button)rootView.findViewById(R.id.statistics);
        onButtonClicked();
     return rootView;
    }

    private void onButtonClicked() {
        createPatternBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onHomeItemSelected(OnFragmentInteractionListener.CREATE_PATTERN);
            }
        });
        loadPatternBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onHomeItemSelected(OnFragmentInteractionListener.LOAD_PATTERN);
            }
        });
        statisticsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onHomeItemSelected(OnFragmentInteractionListener.STATISICS);
            }
        });

    }


    @Override
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


}
