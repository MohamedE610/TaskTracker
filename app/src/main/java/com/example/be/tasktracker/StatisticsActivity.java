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

import java.io.File;
import java.util.ArrayList;

public class StatisticsActivity extends AppCompatActivity {
    private DrawerLayout mDrawer;
    private Toolbar toolbar;
    //private NavigationView nvDrawer;
    private LinearLayout linearLayout;
    private ActionBarDrawerToggle drawerToggle;
    ListView projectsLV, sessionsLV, graphsLV;
    NavProjectAdapter navProjectAdapter;
    NavSessionAdapter navSessionAdapter;
    Button allBT, noneBT;
    // String[]arr={"Project A","Project B","Project C"};
    //  String[]arr2={"Session A","Session B","Session C","Session D","Session E","Session F","Session G","Session I","Session J",};
    String[] arr3 = {"XY Line", "Graph B", "Graph C", "Graph D", "Graph B", "Graph C", "Graph D"};
    ArrayList<Project> projects;
    ArrayList<Session> sessions;
    ArrayList<Session>chosedSessions;
    private Context mContext;
    SelectedProject selectedProject = new SelectedProject();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        mContext = this;
        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        // nvDrawer = (NavigationView) findViewById(R.id.nvView);
        linearLayout = (LinearLayout) findViewById(R.id.nvLinear);
        //setupDrawerContent(linearLayout);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        projects = DataHandler.readProjects(this, new File(this.getFilesDir(), getString(R.string.projects_file_name)));
        sessions = DataHandler.readSessions(this, projects.get(0));
        navProjectAdapter = new NavProjectAdapter(this, DataHandler.readProjectsNames(new File(this.getFilesDir(), getString(R.string.projects_file_name))), selectedProject);
        navSessionAdapter = new NavSessionAdapter(this, sessions);
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
                if(position==0){
                    chosedSessions=navSessionAdapter.getSessions();
                    mDrawer.closeDrawer(GravityCompat.START);
                    XYLineFragment xyLineFragment=new XYLineFragment();
                    xyLineFragment.setSessions(chosedSessions);
                    getSupportFragmentManager().beginTransaction().replace(R.id.nav_layout_container,xyLineFragment).commit();
                    drawerToggle.syncState();
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
