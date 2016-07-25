package com.example.be.tasktracker.Interfaces;

/**
 * Created by BE on 7/25/2016.
 */
public interface StopwatchObserver {
     void onSecondsIncreased(int second);
     void onStopwatchStateChanged(boolean working);

}
