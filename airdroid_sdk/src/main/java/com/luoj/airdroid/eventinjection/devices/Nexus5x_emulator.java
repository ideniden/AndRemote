package com.luoj.airdroid.eventinjection.devices;

import android.view.KeyEvent;

import com.luoj.airdroid.eventinjection.DeviceInput;
import com.luoj.airdroid.eventinjection.Key;


/**
 * Created by 京 on 2016/6/23.
 */
public class Nexus5x_emulator extends AbsDeviceInputDescriptor {

    @Override
    public DeviceInput set() {
        DeviceInput deviceInput=new DeviceInput();
        //按键
        deviceInput.addKey(new Key(KeyEvent.KEYCODE_HOME,"1","1","172"));
        deviceInput.addKey(new Key(KeyEvent.KEYCODE_BACK,"2","1","158"));
        deviceInput.addKey(new Key(KeyEvent.KEYCODE_MENU,"2","1","139"));
        deviceInput.addKey(new Key(KeyEvent.KEYCODE_POWER,"1","1","116"));
        deviceInput.addKey(new Key(KeyEvent.KEYCODE_VOLUME_UP,"1","1","115"));
        deviceInput.addKey(new Key(KeyEvent.KEYCODE_VOLUME_DOWN,"1","1","114"));
        //触摸屏
//        TouchScreen touchScreen=new TouchScreen(
//                new Motion(MotionEvent.ACTION_MOVE,"2","3","35",""),
//                new Motion(MotionEvent.ACTION_MOVE,"2","3","36",""),
//                new Motion(MotionEvent.ACTION_DOWN,"2","3","3a","81"),
//                new Motion(MotionEvent.ACTION_UP,"2","3","3a","0")
//        );
//        deviceInput.setTouchScreen(touchScreen);
        return deviceInput;
    }

}
