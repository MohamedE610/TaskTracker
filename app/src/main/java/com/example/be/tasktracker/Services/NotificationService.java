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
    final int NOTIFY_ID = 1;
    final int NOTIFY_FORGROUND_ID = 2;
    public static final String ServiceAction_START = "START";
    public static final String ServiceAction_STOP_HIDE = "STOP_HIDE";
    public static final String ServiceAction_STOP = "STOP";
    public static final String ServiceAction_NEXT = "NEXT";
    public static final String ServiceAction_PREV = "PREV";
    public static final String ServiceAction_CHOSED = "CHOSED";
    private static boolean alive = false;
    boolean sleep=false;
    long sleepTime=0;
    int secsWas=0;
    long startTime=-1;
    private StringBuilder stringBuilder;
    private NotificationThread mNotificationThread;
    private NotificationManager mNotificationManager;
    private File file;

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
        if (mNotificationManager == null)
            mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (mSessionController == null && SessionController.exists())
            mSessionController = SessionController.getInstance(null);
        String action = intent.getAction();
        switch (action) {
            case ServiceAction_START:
                file=getFile();
                stringBuilder=new StringBuilder();
                alive = true;
                mNotificationThread = new NotificationThread();
                if (!mSessionController.isWorking())
                    mSessionController.setWorking(true);
                startForeground(NOTIFY_ID, getBuilder().build());
                mNotificationThread.start();
                break;
            case ServiceAction_STOP_HIDE:
                if (mNotificationThread != null && mNotificationThread.isAlive())
                    mNotificationThread.interrupt();
                alive = false;
                if (mSessionController.isWorking())
                    mSessionController.setWorking(false);
                try {
                    DataHandler.writeJsonToFile(stringBuilder.toString(),file);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                stopForeground(true);
                stopSelf();

                break;
            case ServiceAction_NEXT:
                break;
            case ServiceAction_PREV:
                break;
            case ServiceAction_CHOSED:
            case ServiceAction_STOP:
                if (mNotificationThread != null && mNotificationThread.isAlive())
                    mNotificationThread.interrupt();
                alive = false;
                if (mSessionController.isWorking())
                    mSessionController.setWorking(false);
                stopForeground(false);
                stopSelf();
                break;

        }

        return START_NOT_STICKY;
        // return super.onStartCommand(intent, flags, startId);
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

            PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
            try {

                while (true) {

                    if(Build.VERSION.SDK_INT <20){
                        if(!pm.isScreenOn()&&!sleep){
                            secsWas=mSessionController.getmSeconds();
                            sleepTime=System.currentTimeMillis();
                            sleep=true;
                            //stringBuilder.append("NOTIFICATION_SERVICE,Thread is still Running time = "+sleepTime+" current seconds "+mSessionController.getmSeconds()+'\n');
                        }
                        else if(sleep&&pm.isScreenOn()){
                            mSessionController.setmSeconds((int) (Math.ceil((double) ((System.currentTimeMillis()-sleepTime)/1000.0))+secsWas));
                            sleep=false;
                          //  stringBuilder.append("NOTIFICATION_SERVICE,Thread is Awake now = "+System.currentTimeMillis()+" Sleep was "+sleepTime +" current seconds "+mSessionController.getmSeconds() +'\n');
                        }
                    }
                    else{
                        if(!pm.isInteractive()&&!sleep){
                            secsWas=mSessionController.getmSeconds();
                            sleepTime=System.currentTimeMillis();
                            sleep=true;
                        }
                        else if(sleep&&pm.isInteractive()){
                            mSessionController.setmSeconds((int) (Math.ceil((double) ((System.currentTimeMillis()-sleepTime)/1000.0))+secsWas));
                            sleep=false;
                        }

                    }
                    Thread.sleep(1000);
                    mSessionController.increase(1);
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
        alive = false;
    }
    public File getFile() {
        File path = Environment.getExternalStorageDirectory();
        File file = new File(path, "test_task_tracker.txt");
        if(!file.exists())
            try {
                file.createNewFile();
                System.out.println("File_Created");
            } catch (IOException e) {
                e.printStackTrace();
            }
        return file;
    }
}
