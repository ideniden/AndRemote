package com.luoj.airdroid.eventinjection;

import android.util.SparseArray;

/**
 * Created by 京 on 2016/6/22.
 */
public class DeviceInput {

    public SparseArray<Key> keys=new SparseArray<>();
    public TouchScreen touchScreen;

    public Key getKey(int keyCode){
        return keys.get(keyCode);
    }

    public void addKey(Key key){
        keys.append(key.keyCode,key);
    }

    public void setTouchScreen(TouchScreen touchScreen){
        this.touchScreen=touchScreen;
    }

    public boolean isSupportTouchScreen(){
        return touchScreen!=null;
    }

    public int getTouchScreenDeviceValue(){
        return touchScreen.device;
    }

    public Motion getMotionX(){
        return touchScreen.move[0];
    }

    public Motion getMotionY(){
        return touchScreen.move[1];
    }

    public Motion getMotionDown(){
        return touchScreen.down;
    }

    public Motion getMotionUp(){
        return touchScreen.up;
    }

}
