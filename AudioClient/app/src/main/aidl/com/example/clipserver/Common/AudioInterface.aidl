// AudioInterface.aidl
package com.example.clipserver.Common;

// Declare any non-default types here with import statements

interface AudioInterface {
    /**
     * Demonstrates some basic types that you can use as parameters
     * and return values in AIDL.
     */

    int getAudioFile(int id);
    void playAudio(int id);
    void pauseAudio();
    void resumeAudio();
    void stopAudio();
    int checkIfStillPlaying();
    void stopService();
    void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat,
            double aDouble, String aString);
}