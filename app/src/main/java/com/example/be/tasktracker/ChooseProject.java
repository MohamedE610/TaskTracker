package com.example.be.tasktracker;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.example.be.tasktracker.Adapters.ProjectsAdapter;
import com.example.be.tasktracker.DataModel.DataHandler;
import com.example.be.tasktracker.DataModel.Project;
import com.example.be.tasktracker.DataModel.Session;

import java.io.File;
import java.util.ArrayList;

public class ChooseProject extends Fragment {
    OnSessionStartListener mListener;
    ListView projectsListView;
    EditText sessionTitleText;
    ProjectsAdapter projectsAdapter;
    ArrayList<Project>projects=new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView= inflater.inflate(R.layout.fragment_choose_project, container, false);
        projectsListView= (ListView) rootView.findViewById(R.id.choose_project_listview);
        sessionTitleText=(EditText)rootView.findViewById(R.id.sessionET);
        projects= DataHandler.readProjects(getActivity(),new File(getActivity().getFilesDir(),getString(R.string.projects_file_name)));
        projectsAdapter=new ProjectsAdapter(getActivity(),projects);
        projectsListView.setAdapter(projectsAdapter);
        projectsAdapter.notifyDataSetChanged();
        onProjectChoosed();
        return rootView;
    }

    private void onProjectChoosed() {
      projectsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
          @Override
          public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
              mListener.onSessionStarted(projects.get(position),sessionTitleText.getText().toString());
          }
      });

    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnSessionStartListener) {
            mListener = (OnSessionStartListener) context;
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
    public interface OnSessionStartListener {
        // TODO: Update argument type and name
        void onSessionStarted(Project project, String s);
    }
}
