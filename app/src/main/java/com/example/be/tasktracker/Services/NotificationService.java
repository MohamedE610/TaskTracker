package com.example.be.tasktracker.Services;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.example.be.tasktracker.DataModel.Session;
import com.example.be.tasktracker.MainActivity;
import com.example.be.tasktracker.R;
import com.example.be.tasktracker.SessionController;
import com.example.be.tasktracker.StopwatchFragment;

/**
 * Created by BE on 7/24/2016.
 */
public class NotificationService extends Service {
    private final IBinder mBinder = new LocalBinder(this);
    private SessionController mSessionController;
    //private boolean firstTime=true;
    final int NOTIFY_ID = 1;
    final int NOTIFY_FORGROUND_ID = 2;
    public static final String ServiceAction_START="START";
    public static final String ServiceAction_STOP_HIDE="STOP_HIDE";
    public static final String ServiceAction_STOP="STOP";
    public static final String ServiceAction_NEXT="NEXT";
    public static final String ServiceAction_PREV="PREV";
    public static final String ServiceAction_CHOSED="CHOSED";
    private static boolean alive=false;
    private NotificationThread mNotificationThread;
    private NotificationManager mNotificationManager;

    public NotificationService(){
        super();
    }

    public static boolean isAlive() {
        return alive;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {return null;}
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if(mNotificationManager==null)
       mNotificationManager= (NotificationManager)getSystemService(Context.NOTIFICATION_SERVICE);
        if(mSessionController==null&&SessionController.exists())
            mSessionController=SessionController.getInstance(null);
        String action=intent.getAction();
        switch (action){
            case ServiceAction_START:
                alive=true;
                mNotificationThread=new NotificationThread();
                startForeground(NOTIFY_ID,getBuilder().build());
                mNotificationThread.start();
                break;
            case ServiceAction_STOP_HIDE:
                if(mNotificationThread!=null && mNotificationThread.isAlive())
                    mNotificationThread.interrupt();
                alive=false;
                stopForeground(true);
                stopSelf();

                break;
            case ServiceAction_NEXT:
                break;
            case ServiceAction_PREV:
                break;
            case ServiceAction_CHOSED:
            case ServiceAction_STOP:
                if(mNotificationThread!=null && mNotificationThread.isAlive())
                    mNotificationThread.interrupt();
                alive=false;
                stopForeground(false);
                stopSelf();
                break;

        }

        return START_NOT_STICKY;
       // return super.onStartCommand(intent, flags, startId);
    }
    public SessionController getSessionController(){
            return mSessionController;
    }
    //add to service
    private NotificationCompat.Builder getBuilder() {
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        notificationIntent.setAction(Intent.ACTION_MAIN);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);


        //  PendingIntent pendingIntent=PendingIntent.getActivity(getActivity(),1,new Intent(getActivity(),NewTaskActivity.class),PendingIntent.FLAG_CANCEL_CURRENT);
        return new NotificationCompat.Builder(this)
                .setContentTitle(mSessionController.getWorkingTaskName())
                .setContentText(StopwatchFragment.convertSecsToText(mSessionController.getmSeconds()))
                .setSmallIcon(R.drawable.icon).setContentIntent(pendingIntent);
    }

    private class NotificationThread extends Thread {
        long seconds;
        NotificationCompat.Builder mNotifyBuilder;


        NotificationThread() {
            super();
            mNotifyBuilder = getBuilder();
        }

        @Override
        public void run() {

            try {

                while (true) {
                    Thread.sleep(1000);
                    mSessionController.increase();
                    mNotifyBuilder.setContentText(StopwatchFragment.convertSecsToText(mSessionController.getmSeconds()));
                    mNotificationManager.notify(
                            NOTIFY_ID,
                            mNotifyBuilder.build());
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }


    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        alive=false;
    }
}
