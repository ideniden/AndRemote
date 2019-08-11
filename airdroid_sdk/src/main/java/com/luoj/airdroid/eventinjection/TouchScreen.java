package com.luoj.airdroid.eventinjection;

import android.view.MotionEvent;

/**
 * Created by 京 on 2016/6/22.
 */
public class TouchScreen {

    public int device=-1;
    public int screenWidth;
    public int screenHeight;
    public Motion[] move=new Motion[2];
    public Motion down;
    public Motion up;

    public TouchScreen(int device,Motion x, Motion y) {
        this.device=device;
        this.move[0] = x;
        this.move[1] = y;
    }

    public Motion[] get(int action){
        if(action == MotionEvent.ACTION_MOVE){
            return move;
        }else if(action == MotionEvent.ACTION_DOWN){
            return new Motion[]{down};
        }else if(action == MotionEvent.ACTION_UP){
            return new Motion[]{up};
        }
        return null;
    }

}
