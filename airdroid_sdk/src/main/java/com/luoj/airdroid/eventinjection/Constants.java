package com.luoj.airdroid.eventinjection;

/**
 * Created by 京 on 2016/7/3.
 */
public class Constants {

    /*  
     * Event types  
     */
    public static final int EV_SYN = 0x00;
    public static final int EV_KEY = 0x01;
    public static final int EV_REL = 0x02;
    public static final int EV_ABS = 0x03;
    public static final int EV_MSC = 0x04;
    public static final int EV_SW = 0x05;
    public static final int EV_LED = 0x11;
    public static final int EV_SND = 0x12;
    public static final int EV_REP = 0x14;

    /*
     * Synchronization events.
     */
    public static final int SYN_REPORT = 0;
    public static final int SYN_CONFIG = 1;
    public static final int SYN_MT_REPORT = 2;


    /*
     * Synchronization events.
     */
    public static final int ABS_MT_TOUCH_MAJOR = 48;
    public static final int ABS_MT_TOUCH_MINOR = 49;
    public static final int ABS_MT_WIDTH_MAJOR = 50;
    public static final int ABS_MT_WIDTH_MINOR = 51;
    public static final int ABS_MT_POSITION_X = 53;
    public static final int ABS_MT_POSITION_Y = 54;
    public static final int ABS_MT_TRACKING_ID = 57;
    public static final int ABS_MT_PRESSURE=58;


    public static final String INPUT_TEXT = "text";
    public static final String INPUT_KEYEVENT = "keyvent";
    public static final String INPUT_TAP = "tap";
    public static final String INPUT_SWIPE = "swipe";

}
