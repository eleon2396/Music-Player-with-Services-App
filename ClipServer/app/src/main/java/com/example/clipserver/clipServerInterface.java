package com.example.clipserver;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.IBinder;
import android.os.RemoteException;
import android.widget.Toast;
import android.support.v4.app.NotificationCompat;

import com.example.clipserver.Common.AudioInterface;

import java.util.ArrayList;

// * Eric Leon eleon23 654889611 9:09 PM
// * CS 478 Project 5
// * This project is all about foreground services. This project consists of two apps:
// * 1) ClipServer: This is a app that holds a service which holds a number of audio clips.
// * 2) AudioClient: This is an app that exposes the functionality for using the ClipServer. This app is able to start and stop the service.
// * The app allows the user to play one of the n audio clips. The user is able to do the following actions:
        // * a. Start the service
        // * b. Play a given clip by number
        // * c. Pause the playback
        // * d. Resume the playback
        // * e. Stop the playback
        // * f. Stop the service
// * NOTE: There are some code snippets that are from Professor Buy's lectures and in class app examples.

public class clipServerInterface extends Service {

    // * Array used for storing the songs that will be used.
    ArrayList<Integer> clipsArray = new ArrayList<>();

    // * Notification ID
    private static final int NOTIFICATION_ID = 1;

    // * Variable for the media player and channel ID
    private MediaPlayer player;
    private String CHANNEL_ID = "com.CS478Proj5";

    private final AudioInterface.Stub mBinder = new AudioInterface.Stub() {

        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        // * Get the audio file that will be playing
        public int getAudioFile(int id){
            int songIndex;
            synchronized (clipsArray){
                songIndex = clipsArray.get(id);
            }
            return songIndex;
        }

        // * play the selected song
        public void playAudio(int id){
            play(id);
        }

        // * pause the song
        public void pauseAudio(){
            pause();
        }

        // * resume the song
        public void resumeAudio(){
            resume();
        }

        //* stop the song
        public void stopAudio(){
            stop();
        }

        // * check if the song is still playing
        public int checkIfStillPlaying(){
            return player.getDuration();
        }

        public void stopService(){
            stopSelf();
        }
    };



    @Override
    public void onCreate(){
        super.onCreate();
        // * initialize the song list
        initializeAudioArray();

    }

    // * once the service is destroyed
    @Override
    public void onDestroy(){
        if(player != null){
            player.stop();
            player.release();
        }
        stopSelf();
    }


    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    // * Initialize the song list
    public void initializeAudioArray(){
        clipsArray.add(R.raw.song1);
        clipsArray.add(R.raw.song2);
        clipsArray.add(R.raw.song3);
        clipsArray.add(R.raw.song4);
        clipsArray.add(R.raw.song5);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startid){

        return START_NOT_STICKY;
    }

    // * Set the song that will play given the selected index from the user
    public void setAudioPlayer(int index){
        if(index == 1){
            player = MediaPlayer.create(this, clipsArray.get(0));
        }
        else if(index == 2){
            player = MediaPlayer.create(this, clipsArray.get(1));
        }
        else if(index == 3){
            player = MediaPlayer.create(this, clipsArray.get(2));
        }
        else if(index == 4){
            player = MediaPlayer.create(this, clipsArray.get(3));
        }
        else{
            player = MediaPlayer.create(this, clipsArray.get(4));
        }
    }

    // * Play the selected song
    private void play(int index){
        // * set the song to be played
        setAudioPlayer(index);
        // * the song is already playing else play the song
        if(player.isPlaying()){
            System.out.println("THE SONG IS CURRENTLY PLAYING");
        }
        else{
            player.start();
        }

        // * if the song is playing
        if(player != null){
            player.setVolume(100, 100);
            player.setLooping(false);

            // * once the song is completed stop
            player.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    System.out.println("THE SONG IS FINISHED");
                    stop();
                }
            });

        }

        // * Intent and pending Intent to create the notification channel and set the Notification
        // * then start the foreground service
        final Intent notifIntent = new Intent();
        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notifIntent, 0);
        this.createNotificationChannel();
        final Notification notification = new 	NotificationCompat.Builder(getApplicationContext(),	CHANNEL_ID)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setOngoing(true).setContentTitle("Music PLaying")
                .setContentText("Click to Access Music Player")
                .setTicker("Music is playing!")
                .setFullScreenIntent(pendingIntent, false).build();

        startForeground(NOTIFICATION_ID, notification);
    }

    // * Create the notification channel
    private	void createNotificationChannel()	{
        //	Create	the	NotificationChannel,	but	only	on	API	26+	because
        //	the	NotificationChannel	class	is	new	and	not	in	the	support	library
        if	(Build.VERSION.SDK_INT >=	Build.VERSION_CODES.O)	{
            CharSequence	name	=	"Music	player	notification";
            String	description	=	"The	channel	for	music	player	notifications";
            int importance	=	NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel	=	new	NotificationChannel(CHANNEL_ID,	name,	importance);
            channel.setDescription(description);
            //	Register	the	channel	with	the	system;	you	can't	change	the	importance
            //	or	other	notification	behaviors	after	this
            NotificationManager	notificationManager	=	getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    // * when the song is paused
    private void pause(){
        if(player.isPlaying()){
            player.pause();
        }
        else{
            Toast.makeText(this, "No song currently playing", Toast.LENGTH_LONG).show();
        }
    }

    // * when the song is to resume
    private void resume(){
        if(player.isPlaying()){
            Toast.makeText(this, "Can't resume a song that's already playing", Toast.LENGTH_LONG).show();
        }
        else{
            player.start();
        }
    }

    // * when to stop the song
    private void stop(){
        player.seekTo(0);
        player.pause();
    }
}
