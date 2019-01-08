package com.chasonc.ndkffmpeg;

public class FFmpeg {

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("ffmpeg");
    }

    static {
        System.loadLibrary("avutil");
        System.loadLibrary("swresample");
        System.loadLibrary("avcodec");
        System.loadLibrary("avformat");
        System.loadLibrary("swscale");
        System.loadLibrary("avfilter");
//        System.loadLibrary("avdevice");
//        System.loadLibrary("ffmpegjni");
    }

    /**
     * ffmpeg_cmd中定义的run方法
     * @param cmd 指令
     * @return 执行code
     */
    public static native int run(String[] cmd);

    public static native int getVideoAngle(String path);
}
