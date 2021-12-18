package com.example.Service.MusicCentralService;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;

import androidx.core.app.NotificationCompat;

import com.example.Service.MusicCommon.MusicCentral;

/*
Songs Used:

Music: 80s Motivational Chiptune by Shane Ivers - https://www.silvermansound.com
Music: Chrome Zone by Shane Ivers - https://www.silvermansound.com
Music: Play The Game by Shane Ivers - https://www.silvermansound.com
Music: broke by Shane Ivers - https://www.silvermansound.com
Music: Kikoskia's Theme by Shane Ivers - https://www.silvermansound.com

* */

public class MusicCentralImpl extends Service {
    private String[] titles;
    private String[] artistNames;
    private String[] urls;
    private Bitmap[] bitmaps;
    private int size;
    private Object lock = new Object();
    private Notification notification;
    private static String CHANNEL_ID = "Music player style";
    private static final int NOTIFICATION_ID = 1;
    private NotificationManager notificationManager;

    public final MusicCentral.Stub mBinder = new MusicCentral.Stub() {
        public Bundle[] getAllInfo(){
            Bundle[] bundleOfInfo = new Bundle[size];

            // when accessing data sync on lock to make thread safe
            synchronized (lock){
                // read data into a bundle for each item
                for(int i = 0; i < bundleOfInfo.length; i++){
                    bundleOfInfo[i] = new Bundle();

                    bundleOfInfo[i].putString("TITLE", titles[i]);
                    bundleOfInfo[i].putString("ARTIST", artistNames[i]);
                    bundleOfInfo[i].putString("URL", urls[i]);
                    bundleOfInfo[i].putParcelable("BITMAP", bitmaps[i]);
                }
            }

            // send out array of bundles
            return bundleOfInfo;
        }

        public Bundle getInfo(int position){
            Bundle bundleOfInfo = new Bundle();

            // when accessing data sync on lock to make thread safe
            synchronized (lock){
                // read data from arrays into bundle
                bundleOfInfo.putString("TITLE", titles[position]);
                bundleOfInfo.putString("ARTIST", artistNames[position]);
                bundleOfInfo.putString("URL", urls[position]);
                bundleOfInfo.putParcelable("BITMAP", bitmaps[position]);
            }

            // send bundle ot
            return bundleOfInfo;
        }

        public String getUrl(int position){
            String url;

            // when accessing data sync on lock to make thread safe
            synchronized (lock){
                url = urls[position];
            }

            return url;
        }
    };

    // when created get all the resources
    @Override
    public void onCreate(){
        super.onCreate();

        // when accessing data sync on lock to make thread safe
        synchronized (lock){
            titles = getResources().getStringArray(R.array.titles);
            artistNames = getResources().getStringArray(R.array.artists);
            urls = getResources().getStringArray(R.array.urls);
            bitmaps = new Bitmap[titles.length];
            bitmaps[0] = BitmapFactory.decodeResource(getResources(),R.drawable.chrome_zone);
            bitmaps[1] = BitmapFactory.decodeResource(getResources(),R.drawable.play_the_game);
            bitmaps[2] = BitmapFactory.decodeResource(getResources(),R.drawable.motivational);
            bitmaps[3] = BitmapFactory.decodeResource(getResources(),R.drawable.broke);
            bitmaps[4] = BitmapFactory.decodeResource(getResources(),R.drawable.kikoskias);
        }

        size = titles.length;
    }

    // Return the Stub defined above
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // UB 11-12-2018:  Now Oreo wants communication channels...
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        CharSequence name = "Music player notification";
        String description = "The channel for music player notifications";
        int importance = NotificationManager.IMPORTANCE_DEFAULT;
        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel(CHANNEL_ID, name, importance);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel.setDescription(description);
        }
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        notificationManager = getSystemService(NotificationManager.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        this.createNotificationChannel();

        // create notification for starting in foreground
        notification = new NotificationCompat.Builder(getApplicationContext(), CHANNEL_ID)
                    .setSmallIcon(android.R.drawable.ic_media_play)
                    .setOngoing(true).setContentTitle("Music Central")
                    .setContentText("Music Central Is Running")
                    .build();

        startForeground(NOTIFICATION_ID, notification);
        // Don't automatically restart this Service if it is killed
        return START_NOT_STICKY;
    }
}