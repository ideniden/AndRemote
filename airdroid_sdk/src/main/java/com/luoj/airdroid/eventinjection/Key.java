package com.luoj.airdroid.eventinjection;

import android.util.SparseArray;
import android.view.KeyEvent;

/**
 * Created by 京 on 2016/6/22.
 */
public class Key {

    public int keyCode;
    public String device;
    public String type;
    public String code;
    public boolean isPhysicalButtons;
    public String x;
    public String y;
    public SparseArray<String> events=new SparseArray<>();

    public Key(int keyCode, String device, String type, String code) {
        this.isPhysicalButtons=true;
        this.keyCode = keyCode;
        this.device = device;
        this.type = type;
        this.code = code;
        events.append(KeyEvent.ACTION_DOWN,"1");
        events.append(KeyEvent.ACTION_UP,"0");
    }

    public Key(int keyCode, String device, String code, String type, String x, String y,String downValue,String upValue) {
        this.isPhysicalButtons=false;
        this.keyCode = keyCode;
        this.device = device;
        this.code = code;
        this.type = type;
        this.x = x;
        this.y = y;
        events.append(KeyEvent.ACTION_DOWN,downValue);
        events.append(KeyEvent.ACTION_UP,upValue);
    }

    public String getEventValue(int actionCode){
        return null!=events&&events.size()>0?events.get(actionCode):null;
    }

}
