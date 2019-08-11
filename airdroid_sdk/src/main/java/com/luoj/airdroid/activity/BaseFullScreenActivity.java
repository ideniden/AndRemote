package com.luoj.airdroid.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.SupportActivity;
import android.view.KeyEvent;
import android.view.View;

/**
 * Created by 京 on 2017/7/12.
 */
public class BaseFullScreenActivity extends BaseActivity implements View.OnSystemUiVisibilityChangeListener {

    private static final int SYSTEM_UI_FLAG_SHOW_FULLSCREEN = 0x00000008;  //View.SYSTEM_UI_FLAG_SHOW_FULLSCREEN

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN);
        getWindow().getDecorView().setSystemUiVisibility(SYSTEM_UI_FLAG_SHOW_FULLSCREEN);
        getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(this);

        hideSystemUI();
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    @Override
    public void onSystemUiVisibilityChange(int visibility) {
        hideSystemUI();
    }

    private void hideSystemUI(){
        getWindow().getDecorView().setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                        | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                        | View.SYSTEM_UI_FLAG_FULLSCREEN
                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_MENU){//jf controller start support activity
            startActivity(new Intent(this, SupportActivity.class));
        }else if(keyCode == KeyEvent.KEYCODE_F2){//jf controller selfview
//            startActivity(new Intent(this,DevicesPreviewActivity.class));
        }
        return super.onKeyUp(keyCode, event);
    }

}
