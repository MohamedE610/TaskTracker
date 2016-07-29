package com.example.be.tasktracker;

import com.example.be.tasktracker.DataModel.Project;
import com.example.be.tasktracker.DataModel.Session;
import com.example.be.tasktracker.Interfaces.StopwatchObserver;

import java.util.ArrayList;

/**
 * Created by BE on 7/25/2016.
 */
public  class SessionController{
    public enum SaveState{SAVED, UPDATES_NOT_SAVED, NOT_SAVED, ABORT_SAVING,NEW}
    private final int STATE_CHANGED=1;
    private final int INCREASED=2;
    private static SessionController sessionController;
    int workingTask;
    SaveState savedState= SaveState.NEW;
    Session mSession;
    int mSeconds;
    boolean working;
    static boolean exist;
    ArrayList<String> tasks;
    ArrayList<StopwatchObserver>observers;
    Project project;
    private SessionController(Project project){
        this.project=project;
        mSession=new Session(project);
        observers=new ArrayList<>();
        tasks=project.getTasks();
    }
    public void registerObserver(StopwatchObserver observer){
        observers.add(observer);
    }
    public void unRegisterObserver(StopwatchObserver observer){
        observers.remove(observer);
    }
    public static SessionController getInstance(Project project){
        if(sessionController==null){
            sessionController=new SessionController(project);
            exist=true;
        }
        return sessionController;
    }
    public int getWorkingTask() {
        return workingTask;
    }

    public void setWorkingTask(int workingTask) {
        if(this.workingTask==workingTask)
            return;

        if(working==false){
            this.workingTask = workingTask;
            mSeconds=(mSession.getTasks().get(tasks.get(workingTask))).intValue();
            return;
        }
        else{
            mSession.getTasks().put(tasks.get(this.workingTask),new Long( mSeconds));
            this.workingTask = workingTask;
            mSeconds=(mSession.getTasks().get(tasks.get(workingTask))).intValue();
            working=false;
            notifyObservers(STATE_CHANGED,working);
        }


    }

    public SaveState getSavedState() {
        return savedState;
    }

    public void setSavedState(SaveState savedState) {
        this.savedState = savedState;
    }

    public Session getmSession() {
        return mSession;
    }

    public void setmSession(Session mSession) {
        this.mSession = mSession;
    }

    public int getmSeconds() {
        return mSeconds;
    }

    /*public void setmSeconds(int mSeconds) {
        this.mSeconds = mSeconds;
        for (int i=0;i<observers.size();i++)
            observers.get(i).onSecondsIncreased(mSeconds);
    }*/
    public void increase(int i){
        mSeconds+=i;
        notifyObservers(INCREASED,false);

    }
    public void setmSeconds(int secs){
        mSeconds=secs;
        notifyObservers(INCREASED,false);
    }

    public boolean isWorking() {
        return working;
    }
    private void notifyObservers(int id,boolean isWorking){
        if(id == STATE_CHANGED){
            for(int i=0;i<observers.size();i++)
                observers.get(i).onStopwatchStateChanged(isWorking);
        }
        else{
            for (int i=0;i<observers.size();i++)
                observers.get(i).onSecondsIncreased(mSeconds);
        }
    }
    public void setWorking(boolean working) {
        if(this.working==working)
            return;
        this.working = working;
        notifyObservers(STATE_CHANGED,working);
        if(working==false)
            mSession.getTasks().put(tasks.get(workingTask),new Long( mSeconds));
        else
            savedState = (savedState == SaveState.SAVED ? SaveState.UPDATES_NOT_SAVED : SaveState.SAVED.NOT_SAVED);



    }
    public static void destroy(){
        sessionController=null;
        exist=false;
    }
    public static boolean exists(){
        return exist;
    }
    public String getWorkingTaskName(){
        return tasks.get(workingTask);
    }
    public Project getProject() {
        return project;
    }

}

