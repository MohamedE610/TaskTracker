package com.example.be.tasktracker;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;

import com.example.be.tasktracker.DataModel.HandleData;
import com.example.be.tasktracker.DataModel.Project;

import java.io.File;
import java.util.ArrayList;

public class ChooseProject extends Fragment {
    OnTaskStartListener mListener;
    ListView projectslist;
    EditText taskTitleText;
    ProjectsAdapter projectsAdapter;
    ArrayList<Project>projects=new ArrayList<>();
    File file;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView= inflater.inflate(R.layout.fragment_choose_project, container, false);
        projectslist= (ListView) rootView.findViewById(R.id.choose_project_listview);
        taskTitleText=(EditText)rootView.findViewById(R.id.taskTitle);
        projects= HandleData.readProjects(getActivity(),new File(getActivity().getFilesDir(),getString(R.string.projects_file_name)));
       // stringArrayAdapter=new ArrayAdapter<String>(getActivity(),R.layout.start_list_item,new String[]{"this","that"});
        projectsAdapter=new ProjectsAdapter(getActivity(),projects);
        projectslist.setAdapter(projectsAdapter);
        projectsAdapter.notifyDataSetChanged();
        return rootView;
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnTaskStartListener) {
            mListener = (OnTaskStartListener) context;
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
    public interface OnTaskStartListener {
        // TODO: Update argument type and name
        void onTaskStarted(String projectTitle);
    }
}
