package com.example.be.tasktracker.DataModel;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by BE on 6/28/2016.
 */
public class Project {
    private String projectName;
    private ArrayList<String>subtasks;
    private long date;
    public   static final  String PROJECT_NAME_KEY="NAME";
    public  static final  String PROJECT_SUBTASKS_KEY="SUBTASKS";
    public  static final  String PROJECT_DATE_KEY="DATE";
    public Project(String projectName, ArrayList<String> subtasks, long date) {
        this.projectName = projectName;
        this.subtasks = subtasks;
        this.date = date;
    }


    public long getDate() {
        return date;
    }

    public void setDate(long date) {
        this.date = date;
    }

    public ArrayList<String> getTasks() {
        return subtasks;
    }

    public void setSubtasks(ArrayList<String> subtasks) {
        this.subtasks = subtasks;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }
/*        JSONObject jsonObject=new JSONObject();
        jsonObject.put(Project.PROJECT_NAME_KEY,getProjectName());
        jsonObject.put(PROJECT_SUBTASKS_KEY,getSubtasks());
        jsonObject.put(PROJECT_DATE_KEY,date);
        return jsonObject;

    }*/

}
