package com.chasonc.ndkffmpeg;

import android.support.annotation.NonNull;

public class Commend {

    @NonNull
    public static String imagesToVideo(String resPath, String savingPath) {
        return "ffmpeg -threads 4 -y -r 20 -i " + resPath + "/image%04d.jpg " + savingPath + "/output/output.mp4";
    }

    @NonNull
    public static String videoToGif(String resPath, String savingPath) {
        return "ffmpeg -i " + resPath + " -vframes 100 -y -f gif -s 480X320 " + savingPath + "/video_100.gif";
    }
}
