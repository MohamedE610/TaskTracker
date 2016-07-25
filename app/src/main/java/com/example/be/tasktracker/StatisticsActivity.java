package com.example.be.tasktracker;

import android.content.Context;
import android.content.res.Configuration;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;

import com.example.be.tasktracker.Adapters.NavProjectAdapter;
import com.example.be.tasktracker.Adapters.NavSessionAdapter;
import com.example.be.tasktracker.DataModel.DataHandler;
import com.example.be.tasktracker.DataModel.Project;
import com.example.be.tasktracker.DataModel.Session;
import com.example.be.tasktracker.Plots.XYLineFragment;
import com.google.gson.Gson;

import java.io.File;
import java.util.ArrayList;

public class StatisticsActivity extends AppCompatActivity {
    public static final String SESSIONS_ARGS = "SESSIONS_ARGS";
    private static final String XYFRAGTAG = "XYFRAGMENT";
    private static final String CHECKED_SESSIONS_KEY = "CHECKED_SEESIONS";
    private static final String CHOSEN_PROJECT_KEY = "CHOSEN_PROJECT";
    public static final String PROJECT_ARG ="PROJECT_ARG" ;
    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    //private NavigationView nvDrawer;
    private LinearLayout linearLayout;
    private ActionBarDrawerToggle drawerToggle;
    ListView projectsLV, sessionsLV, graphsLV;
    NavProjectAdapter navProjectAdapter;
    NavSessionAdapter navSessionAdapter;
    Button allBT, noneBT;
    String[] arr3 = {"XY Line", "Graph B", "Graph C", "Graph D", "Graph B", "Graph C", "Graph D"};
    ArrayList<Project> projects;
    ArrayList<Session> sessions;
    boolean checkedSessions[];
    private Context mContext;
    SelectedProject selectedProject = new SelectedProject();

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBooleanArray(CHECKED_SESSIONS_KEY, checkedSessions);
        outState.putInt(CHOSEN_PROJECT_KEY, selectedProject.getPosition());
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        selectedProject = new SelectedProject();

        mContext = this;
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        // nvDrawer = (NavigationView) findViewById(R.id.nvView);
        linearLayout = (LinearLayout) findViewById(R.id.nvLinear);
        //setupDrawerContent(linearLayout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        projects = DataHandler.readProjects(this, new File(this.getFilesDir(), getString(R.string.projects_file_name)));
        if (savedInstanceState != null) {
            checkedSessions = savedInstanceState.getBooleanArray(CHECKED_SESSIONS_KEY);
            selectedProject = new SelectedProject(savedInstanceState.getInt(CHOSEN_PROJECT_KEY));
        }
        sessions = DataHandler.readSessions(this, projects.get(selectedProject.getPosition()));
        if (checkedSessions == null)
            checkedSessions = new boolean[sessions.size()];


        navProjectAdapter = new NavProjectAdapter(this, DataHandler.readProjectsNames(new File(this.getFilesDir(), getString(R.string.projects_file_name))), selectedProject);
        navSessionAdapter = new NavSessionAdapter(this, sessions, checkedSessions);
        drawerToggle = setupDrawerToggle();
        projectsLV = (ListView) findViewById(R.id.projectslistview);
        sessionsLV = (ListView) findViewById(R.id.sessionslistview);
        graphsLV = (ListView) findViewById(R.id.graphslistview);
        sessionsLV.setAdapter(navSessionAdapter);

        graphsLV.setAdapter(new ArrayAdapter<String>(this, R.layout.subtasks_item, arr3));
        /*listView.setAdapter(new ArrayAdapter<String>(this,R.layout.subtasks_item,arr));*/
        projectsLV.setAdapter(navProjectAdapter);
        graphsLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position == 0) {
                    XYLineFragment xyLineFragment = (XYLineFragment) getSupportFragmentManager().findFragmentByTag(XYFRAGTAG);
                    checkedSessions = navSessionAdapter.getCheckedItems();
                    ArrayList<Session> tempSessions = new ArrayList<>();
                    for (int i = 0; i < checkedSessions.length; i++) {
                        if (checkedSessions[i])
                            tempSessions.add(sessions.get(i));
                    }
                    mDrawer.closeDrawer(GravityCompat.START);
                    drawerToggle.syncState();
                    if (xyLineFragment == null) {
                        xyLineFragment = new XYLineFragment();
                        Bundle bundle = new Bundle();
                        bundle.putString(PROJECT_ARG,(new Gson().toJson(projects.get(selectedProject.getPosition()))));
                        bundle.putString(SESSIONS_ARGS, (new Gson().toJson(tempSessions)));
                        xyLineFragment.setArguments(bundle);
                        getSupportFragmentManager().beginTransaction().replace(R.id.nav_layout_container, xyLineFragment, XYFRAGTAG).commit();
                    } else {
                        xyLineFragment.setSessions(tempSessions);
                        xyLineFragment.setProject(projects.get(selectedProject.getPosition()));
                    }


                }
            }
        });

        allBT = (Button) findViewById(R.id.allBT);
        allBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navSessionAdapter.setAll();
                navSessionAdapter.notifyDataSetChanged();
            }
        });
        noneBT = (Button) findViewById(R.id.noneBT);
        noneBT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                navSessionAdapter.setNone();
                navSessionAdapter.notifyDataSetChanged();
            }
        });
        // Tie DrawerLayout events to the ActionBarToggle
        mDrawer.addDrawerListener(drawerToggle);
    }

    private ActionBarDrawerToggle setupDrawerToggle() {
        return new ActionBarDrawerToggle(this, mDrawer, toolbar, R.string.open, R.string.close);
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
            drawerToggle.syncState();
        } else
            super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // The action bar home/up action should open or close the drawer.
        if (drawerToggle.onOptionsItemSelected(item))
            return true;

        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        // Pass any configuration change to the drawer toggles
        drawerToggle.onConfigurationChanged(newConfig);
    }

    public class SelectedProject {
        int position;

        public SelectedProject() {
        }

        public SelectedProject(int position) {
            this.position = position;
        }

        public void setPosition(int position) {
            this.position = position;
            sessions = DataHandler.readSessions(mContext, projects.get(position));
            navSessionAdapter.setSessions(sessions);
            navSessionAdapter.notifyDataSetChanged();
        }

        public int getPosition() {
            return position;
        }
    }

}
