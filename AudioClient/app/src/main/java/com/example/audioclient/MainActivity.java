package com.example.audioclient;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ResolveInfo;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.RemoteException;
import android.provider.Contacts;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.clipserver.Common.AudioInterface;

import java.net.SocketOption;
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

public class MainActivity extends AppCompatActivity {

    // * Handler for the Main thread
    private class UIHandler extends Handler{
        @Override
        public void handleMessage(Message msg){
            int what = msg.what;
            switch (what) {
                case ChangeUI:
                    // * display the song name to the UI
                    songName.setText(getSongName(toNum));
                    break;
                case songEnded:
                    Toast.makeText(getApplicationContext(), "THE SONG HAS ENDED", Toast.LENGTH_LONG).show();

                    break;
            }

        }
    }
    // * variable to just hold the tag for the Clip Server
    protected static final String TAG ="ClipServerUser";

    // * List used for storing the songs to show which song has been selected
    ArrayList<Integer> songList = new ArrayList<>();

    // * Instance of the Audio Interface
    private AudioInterface mAudioInterface;

    // * Boolean used to check if the service is bound
    private Boolean checkBound = false;

    // * flag used to check when the song is finished
    private int checkIsPlaying = 0;

    // * These are the variables for the View elements that will be used during the project.
    private Button startService;
    private Button playClip;
    private Button pausePlayback;
    private Button resumePlayback;
    private Button stopPlayback;
    private Button stopService;
    private EditText enterNum;
    private TextView songName;

    // * Intent used to bind
    private Intent i;

    // * Messages for thread and UI handler
    private final int startServ = 1;
    private final int clipPlayer = 2;
    private final int pausePlayer = 3;
    private final int resumePlayer = 4;
    private final int stopPlaybackPlayer = 5;
    private final int stopServicePlayer = 6;
    private final int ChangeUI = 7;
    private final int songEnded = 8;

    // * variable to hold the user input to integer
    private int toNum = 0;

    // * UI handler and thread
    private UIHandler mainHandler = new UIHandler();
    private Worker1 thread1 = new Worker1(mainHandler);

    // * Set up the connection to the service Clip Server
    private final ServiceConnection mConnection = new ServiceConnection() {

        // * Once the service is connected set the interface and update the checkBound variable
        public void onServiceConnected(ComponentName className, IBinder iservice) {
            mAudioInterface = AudioInterface.Stub.asInterface(iservice);
            checkBound = true;

        }
        // * Once the service is disconnected, update the audio interface and checkBound variable
        public void onServiceDisconnected(ComponentName className) {
            mAudioInterface = null;
            checkBound = false;
        }
    };

    // * CURRENTLY NOT BEING USED....
    // * function used to initialize song list.
    private void setSongList(){
        songList.add(R.raw.song1);
        songList.add(R.raw.song2);
        songList.add(R.raw.song3);
        songList.add(R.raw.song4);
        songList.add(R.raw.song5);

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // * Get the variables from the View elements and be able to reference them
        startService = (Button)findViewById(R.id.startServiceButton);
        playClip = (Button)findViewById(R.id.playClipbutton);
        pausePlayback = (Button)findViewById(R.id.pausePlaybackButton);
        resumePlayback = (Button)findViewById(R.id.resumePlaybackButton);
        stopPlayback = (Button)findViewById(R.id.stopPlaybackButton);
        stopService = (Button)findViewById(R.id.stopServiceButton);
        enterNum = (EditText)findViewById(R.id.enterClipNumberEditText);
        songName = (TextView)findViewById(R.id.displaySongTextView);

        // * Initialize the song list
        setSongList();

        // * Disable all the buttons except for
        disableOrEnableButtons(false);

        // * start thread
        thread1.start();

        // * On Click listener for the start service button
        startService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // * set specific buttons enable or disabled.
                // * enable the play clip button
                playClip.setEnabled(true);
                // * disable the start service button
                startService.setEnabled(false);
                // * enable the enter number text view
                enterNum.setEnabled(true);
                // * enable the stop stop service button
                stopService.setEnabled(true);

                // * Call thread
                Message m = thread1.handlerWorker1.obtainMessage(startServ);
                thread1.handlerWorker1.sendMessage(m);

            }
        });

        // * Play clip click listener
        playClip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                System.out.println(checkBound);
                // * string to user input
                String userInput;
                userInput = enterNum.getText().toString();

                // * check if something was entered, if not then toat error message
                if(userInput.isEmpty()){
                    Toast.makeText(getApplicationContext(), "The clip number is empty...", Toast.LENGTH_LONG).show();
                }
                // * else something was entered.
                else {
                    // * Convert to int
                    toNum = Integer.parseInt(userInput);
                    // * Error checking
                    System.out.println("THE SONG NUMBER IS: " + userInput);
                    System.out.println("THE SONG NUMBER IS: " + userInput + " AS A NUM");

                    // * make sure the user input was between 1 and 5 if not then toast error message
                    if (toNum < 1 || toNum > 5) {
                        Toast.makeText(getApplicationContext(), "The clip number should be between 1 and 5...", Toast.LENGTH_LONG).show();
                    }
                    // * else it was correct and start the song
                    else{
                        // * enable to the pause playback
                        pausePlayback.setEnabled(true);
                        // * disable the play clip button and text view for user input
                        playClip.setEnabled(false);
                        enterNum.setEnabled(false);
                        // * enable the stop playback button
                        stopPlayback.setEnabled(true);
                        // * clear the textview and set the text to blank
                        enterNum.clearFocus();
                        enterNum.setText("");


                        // * call thread
                        Message m = thread1.handlerWorker1.obtainMessage(clipPlayer);
                        thread1.handlerWorker1.sendMessage(m);

                    }

                }

            }
        });

        // * set the pause playback click listener
        pausePlayback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // * disable the pause playback button
                pausePlayback.setEnabled(false);
                // * enable the resume playback button
                resumePlayback.setEnabled(true);
                // * call thread
                Message m = thread1.handlerWorker1.obtainMessage(pausePlayer);
                thread1.handlerWorker1.sendMessage(m);
            }
        });

        // * set the resume playback click listener
        resumePlayback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // * disable the resume playback button
                resumePlayback.setEnabled(false);
                // * enable the pause playback button
                pausePlayback.setEnabled(true);
                // * call thread
                Message m = thread1.handlerWorker1.obtainMessage(resumePlayer);
                thread1.handlerWorker1.sendMessage(m);
            }
        });

        // * stop playback click listener
        stopPlayback.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println(checkBound);
                // * disable the pause playback and resume playback buttons
                pausePlayback.setEnabled(false);
                resumePlayback.setEnabled(false);

                // * enable the playclip buttons and enter number view.
                playClip.setEnabled(true);
                enterNum.setEnabled(true);

                // * Set the current song playing text view
                songName.setText("");


                // * disable the stop playback button
                stopPlayback.setEnabled(false);


                // * call thread
                Message m = thread1.handlerWorker1.obtainMessage(stopPlaybackPlayer);
                thread1.handlerWorker1.sendMessage(m);
            }
        });

        // * Stop service button click listener
        stopService.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // * set the current song playing to nothing
                songName.setText("");
                // * Toast that the service will be stopped
                Toast.makeText(getApplicationContext(),  "STOPPING SERVICE", Toast.LENGTH_LONG).show();
                // * set the enable start service button
                startService.setEnabled(true);
                // * disable the rest of the buttons like when the apps starts
                disableOrEnableButtons(false);
                // * set the check bound
                checkBound = false;

                // * call thread
                Message m = thread1.handlerWorker1.obtainMessage(stopServicePlayer);
                thread1.handlerWorker1.sendMessage(m);


            }
        });

    }

    private synchronized void playClipFunction(){
        // * try and catch to play the song.
        try {
            // * Check if the service is bound, if it is then play the song, else throw and catch error
            if (checkBound) {
                mAudioInterface.playAudio(toNum);
            }
            else {
                Log.i(TAG, "Ugo says that the service was not bound!");
            }
        }
        catch(Exception e){
            Log.e(TAG, e.toString());
        }
    }

    private synchronized void pausePlaybackFunction(){
        // * try and catch to pause the audio
        try {
            mAudioInterface.pauseAudio();
        }
        catch (RemoteException e){
            Log.e(TAG, e.toString());
        }
    }

    private synchronized void resumePlaybackFunction(){
        // * try and catch to resume the audio
        try{
            mAudioInterface.resumeAudio();
        }
        catch(RemoteException e){
            Log.e(TAG, e.toString());
        }
    }

    private synchronized void stopPlaybackFunction(){
        // * Try and catch for stopping the audio
        try{
             mAudioInterface.stopAudio();
             if(!checkBound) {
                 unbindService(this.mConnection);
             }
        }
        catch(RemoteException e){
            Log.e(TAG, e.toString());
        }
    }


    private synchronized void stopServiceFunction(){
        try {
            if(checkBound) {
                mAudioInterface.stopAudio();
            }
            unbindService(mConnection);
            mAudioInterface.stopService();
        }
        catch(RemoteException e){
            Log.e(TAG, e.toString());
        }
    }

    // * Method to get the song name to be displayed
    private String getSongName(int index){
        if(index == 1){
            return "Antonio Martín y Coll: El Villano & Danza del Hacha";
        }
        else if(index == 2){
            return "Beethoven: Moonlight Sonata";
        }
        else if(index == 3){
            return "Beethoven: Symphony No. 5";
        }
        else if(index  == 4){
            return "Vivaldi: Four Seasons";
        }
        else if(index == 5){
            return "Francisco Tárrega: Variaciones sobre El Carnaval de Venecia de Paganini";
        }
        else{
            return "";
        }
    }

    // * once the app is destroyed just make sure the service is unbind
    @Override
    protected void onDestroy(){
        super.onDestroy();
        unbindService(this.mConnection);
        checkBound = false;
    }

    // * disable certain buttons at the beginning of the app
    private void disableOrEnableButtons(Boolean check){
        playClip.setEnabled(check);
        pausePlayback.setEnabled(check);
        resumePlayback.setEnabled(check);
        stopPlayback.setEnabled(check);
        stopService.setEnabled(check);
        enterNum.setEnabled(check);
    }


    // * function to start the service
    private void startServiceFunction(){
        // * check if the activity is NOT bound if so then bind it
        if (!checkBound) {
            // * set the intent to get the interface class name
            boolean b = false;
            i = new Intent(AudioInterface.class.getName());
            System.out.println(AudioInterface.class.getName());

            // UB:  Stoooopid Android API-20 no longer supports implicit intents
            // to bind to a service #@%^!@..&**!@
            // Must make intent explicit or lower target API level to 19.
            // * get the package name
            ResolveInfo info = getPackageManager().resolveService(i, 0);
            System.out.println(info.serviceInfo.packageName);
            // * set the component
            i.setComponent(new ComponentName(info.serviceInfo.packageName, info.serviceInfo.name));

            // * bind the service
            b = bindService(i, this.mConnection, Context.BIND_AUTO_CREATE);

            // * check if the service was bounded correctly and display the results of the binding process
            if (b) {
                Toast.makeText(getApplicationContext(), "BOUNDED CORRECTLY", Toast.LENGTH_LONG ).show();
                Log.i(TAG, "Ugo says bindService() succeeded!");
            } else {
                Toast.makeText(getApplicationContext(), "BOUNDED FAILED", Toast.LENGTH_LONG ).show();
                Log.i(TAG, "Ugo says bindService() failed!");
            }
        }

    }

    // Bind to KeyGenerator Service
    @Override
    protected void onResume() {
        super.onResume();

    }

    // * Worker thread 1, this worker's thread strategy to find the gopher hole is just to guess at random
    private class Worker1 extends Thread{
        // * Handler reference to the main handler and make a reference handler for worker1
        public Handler handlerWorker1;
        private UIHandler mainHandler;

        public Worker1(UIHandler h){
            mainHandler = h;
        }

        @SuppressLint("HandlerLeak")
        public void run(){
            Looper.prepare();
            handlerWorker1 = new Handler(){
                @Override
                public void handleMessage(Message msg){
                    int what = msg.what;
                    switch(what){
                        case startServ:
                            startServiceFunction();
                            break;
                        case clipPlayer:
                            playClipFunction();
                            Message m = mainHandler.obtainMessage(ChangeUI);
                            mainHandler.sendMessage(m);

                            break;
                        case pausePlayer:
                            pausePlaybackFunction();
                            break;
                        case resumePlayer:
                            resumePlaybackFunction();
                            break;
                        case stopPlaybackPlayer:
                            stopPlaybackFunction();
                            break;
                        case stopServicePlayer:
                            stopServiceFunction();
                            break;
                    }
                }
            }; // * End of Handler
            Looper.loop();
        }

    }

}
