package com.example.be.tasktracker.DataModel;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by BE on 6/28/2016.
 */
public class Project {
    private String projectName;
    private ArrayList<String>subtasks;
    private long date;

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

    public ArrayList<String> getSubtasks() {
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


}
