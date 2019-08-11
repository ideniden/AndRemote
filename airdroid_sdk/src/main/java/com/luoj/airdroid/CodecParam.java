package com.luoj.airdroid;

import android.content.pm.ActivityInfo;
import android.media.MediaFormat;

public class CodecParam {

    public static int oritation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

    public static int width = 720;
    public static int height = 1280;
    public static int dpi = 1;
    public static int bitrate = (1024 * 1024 * 2);

    public static String mime = MediaFormat.MIMETYPE_VIDEO_AVC;
    public static int framerate = 30;
    public static int i_frame_interval = 1;

    public static int TIMEOUT_USEC = 10000;

    public static void setOritationLandscape() {
        oritation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
        int tmp = height;
        height = width;
        width = tmp;
    }

}
