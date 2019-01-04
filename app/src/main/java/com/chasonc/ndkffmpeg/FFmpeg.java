package com.chasonc.ndkffmpeg;

public class FFmpeg {

    /**
     * ffmpeg_cmd中定义的run方法
     * @param cmd 指令
     * @return 执行code
     */
    public static native int run(String[] cmd);

    public static native int getVideoAngle(String path);
}
