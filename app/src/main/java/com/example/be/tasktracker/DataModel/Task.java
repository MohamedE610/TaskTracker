package com.example.be.tasktracker.DataModel;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by BE on 7/3/2016.
 */
public class Task {
    transient Project project;
    HashMap<String,Long>subtasks;
    String Title;

    public Long getDateInMs() {
        return dateInMs;
    }

    public void setDateInMs(Long dateInMs) {
        this.dateInMs = dateInMs;
    }

    Long dateInMs;

    public Task(Project project){
        this.project=project;
        int x=project.getSubtasks().size();
        subtasks=new HashMap<String, Long>(x);
        dateInMs=System.currentTimeMillis();
        for(int i=0;i<x;i++)
            subtasks.put(project.getSubtasks().get(i),new Long(0));
    }
    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
    public Long getSubtask(String str) {
        return subtasks.get(str);
    }
    public String getTitle() {
        return Title;
    }

    public void setTitle(String title) {
        Title = title;
    }

    public HashMap<String, Long> getSubtasks() {
        return subtasks;
    }

    public void setSubtasks(HashMap<String, Long> subtasks) {
        this.subtasks = subtasks;
    }
}
