package com.example.be.tasktracker.Services;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

/**
 * Created by BE on 7/25/2016.
 */
public class LocalBinder extends Binder {
    final NotificationService notificationService;

    public LocalBinder(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    public NotificationService getService() {
        // Return this instance of LocalService so clients can call public methods
        return notificationService;
    }


}