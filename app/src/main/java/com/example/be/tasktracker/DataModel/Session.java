package com.example.be.tasktracker.DataModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;

/**
 * Created by BE on 7/3/2016.
 */
public class Session {
    transient Project project;
    HashMap<String,Long>tasks;
    String Title;
    Long dateInMs;
    public Session(Project project){
        this.project=project;
        int x=project.getTasks().size();
        tasks=new HashMap<String, Long>(x);
        dateInMs=System.currentTimeMillis();
        for(int i=0;i<x;i++)
            tasks.put(project.getTasks().get(i),new Long(0));
    }
    public Project getProject() {
        return project;
    }

    public void setProject(Project project) {
        this.project = project;
    }
    public Long getTaskTime(String str) {
        return tasks.get(str);
    }
    public String getTitle() {
        return Title;
    }
   public String[] getTasksNames(){
       String[]names=new String[tasks.size()];
      // tasks.values();
         tasks.keySet().toArray(names);
       return names;

   }
    public void setTitle(String title) {
        Title = title;
    }

    public HashMap<String, Long> getTasks() {
        return tasks;
    }

    public void setSubtasks(HashMap<String, Long> subtasks) {
        this.tasks = subtasks;
    }

    public Long getDateInMs() {
        return dateInMs;
    }

    public void setDateInMs(Long dateInMs) {
        this.dateInMs = dateInMs;
    }


    @Override
    public boolean equals(Object o) {
        if(o instanceof Session)
            return (((Session) o).getDateInMs().equals(this.dateInMs));

        return false;
    }
}
