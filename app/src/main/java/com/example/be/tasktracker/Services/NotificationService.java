package com.example.be.tasktracker.Services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.example.be.tasktracker.DataModel.DataHandler;
import com.example.be.tasktracker.DataModel.Session;
import com.example.be.tasktracker.MainActivity;
import com.example.be.tasktracker.R;
import com.example.be.tasktracker.SessionController;
import com.example.be.tasktracker.StopwatchFragment;

import java.io.File;
import java.io.IOException;

/**
 * Created by BE on 7/24/2016.
 */
public class NotificationService extends Service {
    private final IBinder mBinder = new LocalBinder(this);
    private SessionController mSessionController;
    //private boolean firstTime=true;
    public static final int NOTIFY_ID = 1;
    final int NOTIFY_FORGROUND_ID = 2;
    public static final String ServiceAction_START = "START";
    public static final String ServiceAction_STOP_HIDE = "STOP_HIDE";
    public static final String ServiceAction_STOP = "STOP";
    public static final String ServiceAction_NEXT = "NEXT";
    public static final String ServiceAction_PREV = "PREV";
    public static final String ServiceAction_CHOSED = "CHOSED";
    private static boolean alive = false;
    private StringBuilder stringBuilder;
    private NotificationThread mNotificationThread;
    Intent playIntent;
    Intent stopIntent;
    Intent nextIntent;
    Intent preIntent;
    private NotificationCompat.Builder builder;
    private File file;
    private PendingIntent pendingPlayIntent;
    private PendingIntent pendingStopIntent;
    private PendingIntent pendingNextIntent;
    private PendingIntent pendingPreIntent;
    private String action;

    public NotificationService() {
        super();
    }

    public static boolean isAlive() {
        return alive;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {


        if (mSessionController == null && SessionController.exists())
            mSessionController = SessionController.getInstance(null);

        action = intent.getAction();
        intializeIntents();
        builder = getBuilder();
        startForeground(NOTIFY_ID, builder.build());
        switch (action) {

            case ServiceAction_PREV:
                mSessionController.setWorkingTask(((mSessionController.getWorkingTask() + mSessionController.getTasksCount() - 1) % mSessionController.getTasksCount()));
                mSessionController.setWorking(false);
                builder = getBuilder();
                ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(
                        NotificationService.NOTIFY_ID,
                        builder.build());
                break;

            case ServiceAction_NEXT:
                mSessionController.setWorkingTask(((mSessionController.getWorkingTask() + 1) % mSessionController.getTasksCount()));
                mSessionController.setWorking(false);
                builder = getBuilder();
                ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).notify(
                        NotificationService.NOTIFY_ID,
                        builder.build());
                break;

            case ServiceAction_STOP_HIDE:
                mSessionController.setWorking(false);
                stopForeground(true);
                ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).cancel(NotificationService.NOTIFY_ID);


                break;
            case ServiceAction_START:
                mNotificationThread = NotificationThread.getInstance(builder, this, mSessionController);
             //   if(mNotificationThread.isNewInstance())

                if (mNotificationThread.isAlive()) {
                    mNotificationThread.interrupt();
                    try {
                        mNotificationThread.join();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    mNotificationThread = NotificationThread.getInstance(builder, this, mSessionController);
                }
                alive = true;
                mSessionController.setWorking(true);
                mNotificationThread.start();
                break;

            case ServiceAction_STOP:
                mSessionController.setWorking(false);
                stopForeground(false);
                break;

        }

        return START_NOT_STICKY;
        // return super.onStartCommand(intent, flags, startId);
    }


    private void intializeIntents() {
        playIntent = new Intent(this, NotificationService.class);
        stopIntent = new Intent(this, NotificationService.class);
        nextIntent = new Intent(this, NotificationService.class);
        preIntent = new Intent(this, NotificationService.class);
        playIntent.setAction(ServiceAction_START);
        stopIntent.setAction(ServiceAction_STOP);
        nextIntent.setAction(ServiceAction_NEXT);
        preIntent.setAction(ServiceAction_PREV);
        pendingPlayIntent = PendingIntent.getService(this, 0, playIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        pendingStopIntent = PendingIntent.getService(this, 0, stopIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        pendingNextIntent = PendingIntent.getService(this, 0, nextIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        pendingPreIntent = PendingIntent.getService(this, 0, preIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
    }

    public SessionController getSessionController() {
        return mSessionController;
    }

    //add to service
    private NotificationCompat.Builder getBuilder() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        // Intent play = new Intent(this, NotificationService.class);
        //startIntent.setAction(NotificationService.ServiceAction_START);


        //  PendingIntent pendingIntent=PendingIntent.getActivity(getActivity(),1,new Intent(getActivity(),NewTaskActivity.class),PendingIntent.FLAG_CANCEL_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setContentTitle(mSessionController.getWorkingTaskName())
                .setContentText(StopwatchFragment.convertSecsToText(mSessionController.getmSeconds()))
                .setSmallIcon(R.drawable.icon).setContentIntent(pendingIntent)
                .setCategory(MEDIA_SESSION_SERVICE)
                .addAction(R.drawable.previousnotification, null, pendingPreIntent);
        if (action == ServiceAction_START)
            builder.addAction(R.drawable.stopnotification, null, pendingStopIntent);
        else
            builder.addAction(R.drawable.playnotification, null, pendingPlayIntent);
        builder.addAction(R.drawable.nextnotification, null, pendingNextIntent);
        return builder;


    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        alive = false;
    }

    public File getFile() {
        File path = Environment.getExternalStorageDirectory();
        File file = new File(path, "test_task_tracker.txt");
        if (!file.exists())
            try {
                file.createNewFile();
                System.out.println("File_Created");
            } catch (IOException e) {
                e.printStackTrace();
            }
        return file;
    }
}

class NotificationThread extends Thread {
    private static boolean newInstance;
    private NotificationManager mNotificationManager;
    boolean sleep = false;
    long sleepTime = 0;
    int secsWas = 0;
    private NotificationCompat.Builder mNotifyBuilder;
    private static NotificationThread mNotificationThread;
    private NotificationService mNotificationService;
    private SessionController mSessionController;
    private static boolean dead = true;

    public static boolean isNewInstance() {
        return newInstance;
    }

    public static NotificationThread getInstance(NotificationCompat.Builder notifyBuilder, NotificationService service, SessionController sessionController) {

        if (mNotificationThread == null){
            mNotificationThread = new NotificationThread(notifyBuilder, service, sessionController);
            newInstance=true;
        }
        else
            newInstance=false;

        return mNotificationThread;
    }


    private NotificationThread(NotificationCompat.Builder notifyBuilder, NotificationService service, SessionController sessionController) {
        super();
        mNotifyBuilder = notifyBuilder;
        mNotificationService = service;
        mSessionController = sessionController;
        mNotificationManager = (NotificationManager) service.getSystemService(Context.NOTIFICATION_SERVICE);


    }

    @Override
    public void run() {
        dead = false;
        PowerManager pm = (PowerManager) mNotificationService.getSystemService(Context.POWER_SERVICE);
        try {

            while (mSessionController.isWorking()) {

                if (Build.VERSION.SDK_INT < 20) {
                    if (!pm.isScreenOn() && !sleep) {
                        secsWas = mSessionController.getmSeconds();
                        sleepTime = System.currentTimeMillis();
                        sleep = true;
                        //stringBuilder.append("NOTIFICATION_SERVICE,Thread is still Running time = "+sleepTime+" current seconds "+mSessionController.getmSeconds()+'\n');
                    } else if (sleep && pm.isScreenOn()) {
                        mSessionController.setmSeconds((int) (Math.ceil((double) ((System.currentTimeMillis() - sleepTime) / 1000.0)) + secsWas));
                        sleep = false;
                        //  stringBuilder.append("NOTIFICATION_SERVICE,Thread is Awake now = "+System.currentTimeMillis()+" Sleep was "+sleepTime +" current seconds "+mSessionController.getmSeconds() +'\n');
                    }
                } else {
                    if (!pm.isInteractive() && !sleep) {
                        secsWas = mSessionController.getmSeconds();
                        sleepTime = System.currentTimeMillis();
                        sleep = true;
                    } else if (sleep && pm.isInteractive()) {
                        mSessionController.setmSeconds((int) (Math.ceil((double) ((System.currentTimeMillis() - sleepTime) / 1000.0)) + secsWas));
                        sleep = false;
                    }

                }
                Thread.sleep(1000);
                if (mSessionController.isWorking()) {
                    mSessionController.increase(1);
                    mNotifyBuilder.setContentText(StopwatchFragment.convertSecsToText(mSessionController.getmSeconds()));
                    mNotificationManager.notify(
                            NotificationService.NOTIFY_ID,
                            mNotifyBuilder.build());
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            clear();
        }
    }

    private void clear() {
        dead = true;
        mNotifyBuilder = null;
        mNotificationService = null;
        mSessionController = null;
        mNotificationManager = null;
        mNotificationThread = null;
    }


}